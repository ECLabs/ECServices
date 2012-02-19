package ecservices.controllers

import java.text.DateFormat
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletResponse

import grails.converters.JSON
import grails.converters.XML
import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.Method.GET
import static groovyx.net.http.ContentType.TEXT
import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.ContentType.XML
import ecservices.Task;
import static groovyx.net.http.ContentType.URLENC
import groovy.time.TimeCategory
import groovy.util.slurpersupport.GPathResult
import groovy.util.slurpersupport.NodeChild
import groovy.xml.StreamingMarkupBuilder

// http://localhost:8080/ECServices/mytasks
class MytasksController {
	
	@PostConstruct
	def initialize() {
		println "############### initialize() in MytasksController"
	}
	
	@PreDestroy
	def cleanUp() {
		println "############### cleanUp()  - Shutting down MytasksController"
	}
	
	// Google Sites API Oauth2
	def NETWORK_NAME = "Google"
	def CLIENT_ID = "690667165235-seq5tjj5vfsr7cd7irfjrdoecifjsf40.apps.googleusercontent.com"
	def CLIENT_SECRET = "jcYwNY7Y_8MTMyT27GyRgy7g"
	def REDIRECT_URI = "http://localhost:8080/ECServices/mytasks/getTasks"
	def SCOPE = "https://sites.google.com/feeds/"
	def RESPONSE_TYPE = "code"
	def AUTHORIZE_URL = "https://accounts.google.com/o/oauth2/auth?client_id=${CLIENT_ID}&redirect_uri=${REDIRECT_URI}&scope=${SCOPE}&response_type=${RESPONSE_TYPE}";
	def API_KEY = "AIzaSyCmCwkxWuUuSSOSneMPBA3vPF2UWNfwr_E"
	def PROTECTED_RESOURCE_URL = "https://sites.google.com";

	def index = {
		
		System.out.println("=== " + NETWORK_NAME + "'s OAuth Workflow ===");
		session.googleAccessToken = null
		if(session.googleAccessToken == null) {
			redirect(url:AUTHORIZE_URL)
		}
		else {
			println "redirecting to getTasks"
			redirect(action:getTasks)
		}
		
	}
	
	def testing = {
		render "hello world"
	}
	
	def getTasks = {
		if(session.googleAccessToken == null)
		{
			println 'werwerwerwerwerwere' + params['code']
			if(params['code'] != null) {
				session.verifierCodeGoogle = params['code']
				
				// get access and refresh tokens
				def http = new HTTPBuilder( 'https://accounts.google.com/o/oauth2/'  )
				def postBody = [code:session.verifierCodeGoogle,client_id:CLIENT_ID,client_secret:CLIENT_SECRET,redirect_uri:REDIRECT_URI,grant_type:'authorization_code'] // will be url-encoded
				
				http.post( path: 'token', body: postBody, requestContentType: URLENC ) { resp, json ->
				  println "response status: ${resp.statusLine}"
				  assert resp.statusLine.statusCode == 200
				  session.googleRefreshToken = json.refresh_token
				  session.googleAccessToken = json.access_token
				  use(TimeCategory) {
					  session.googleExpiryDate = json.expires_in.seconds.from.now
				  }
				  println "Token expires at ${session.googleExpiryDate}"
				}
			}
			else
				redirect(action:index)
		}
				  
		println session.googleExpiryDate
		
		def feedLink = ''
		
		def http2 = new HTTPBuilder(PROTECTED_RESOURCE_URL)
		http2.request(GET, groovyx.net.http.ContentType.XML) { req ->
			uri.path = '/feeds/content/evanschambers.com/marketing'
			uri.query = [access_token:session.googleAccessToken, path:'/home/action-items']
			headers.'Accept' = 'application/atom+xml'
			headers.'GData-Version' =  '1.4'
			// TODO: try and get the headers to work instead of passing the token in the uri
			// headers.'Authorization' = 'GoogleLogin auth=' + session.googleAccessToken
			response.success = { resp, data ->
				println printNode(data)

				println resp.status
				println "printing node:" + data.name()
				// iterate over each XML 'status' element in the response:
			    feedLink =  data.entry.feedLink.@href
				println feedLink
				
				
				
				//render "hello world"
			}
		}
		/* TODO
		URL.metaClass.queryAsMap = {
		  if(!delegate.query) return [:]
		
		  delegate.query.split('&').inject([:]) {map, kv ->
		    def (key, value) = kv.split('=').toList()
		    if(value != null) {
		      map[key] = URLDecoder.decode(value)
		    }
		    return map
		  }
		}
		*/
		URL feedURL = feedLink.toURL()
		// TODO: move this query-to-map logic to a metaclass on URL
		def map = feedURL.query.split('&').inject([:]) {map, kv-> def (key, value) = kv.split('=').toList(); map[key] = value != null ? URLDecoder.decode(value) : null; map }
		map['access_token'] = session.googleAccessToken
		render map
		
		def tasks = []
		http2.request(GET, groovyx.net.http.ContentType.XML) { req ->
			uri.path = '/feeds/content/evanschambers.com/marketing'
			uri.query = map
			headers.'Accept' = 'application/atom+xml'
			headers.'GData-Version' =  '1.4'
			// TODO: try and get the headers to work instead of passing the token in the uri
			// headers.'Authorization' = 'GoogleLogin auth=' + session.googleAccessToken
			response.success = { resp, data ->
				println printNode(data)
				println resp.status
				
				//.findAll{ it.@make.text().contains('e') }
				def rowNum = 0
				data.entry.each {
					Task task = new Task()
					task.createDate = it.published.text()
					task.owner = it.field.findAll { it.@name.text().contains('Owner')}[0]
					task.description = it.field.findAll { it.@name.text().contains('Description')}[0]
					task.resolution = it.field.findAll { it.@name.text().contains('Resolution')}[0]
					task.complete = it.field.findAll { it.@name.text().contains('Complete')}[0]
					task.dueDate = it.field.findAll { it.@name.text().contains('Due Date')}[0]
					tasks.push task
					//render "hello"
				}
				render tasks as JSON
			}
			
		}
		
	}
	/**
	 * printNode - Prints the xml for a given node
	 * @param node
	 * @return
	 */
	def printNode(NodeChild node) {
		def writer = new StringWriter()
		writer << new StreamingMarkupBuilder().bind {
			mkp.declareNamespace('':node[0].namespaceURI())
			mkp.yield node
		}
		new XmlNodePrinter().print(new XmlParser().parseText(writer.toString()))
	}
	
	def private findMarketingTasks() {
		def http2 = new HTTPBuilder(PROTECTED_RESOURCE_URL)
		http2.request(GET, TEXT) { req ->
			uri.path = '/feeds/content/evanschambers.com/marketing'
			headers.'GData-Version' =  '1.4'
			response.success = { resp, reader ->
				assert resp.statusLine.statusCode == 200
			}
		}
	}
	
}
