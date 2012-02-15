package ecservices.services

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.ContentType.TEXT
import static groovyx.net.http.Method.GET

class MytasksService {
	
	static transactional = true
	
	@PostConstruct
	def initialize() {
		println "############### initialize() in MytasksService"
		//graphDb = new EmbeddedGraphDatabase( DB_PATH );
		//registerShutdownHook();
		
	}
	
	@PreDestroy
	def cleanUp() {
		println "############### cleanUp()  - Shutting down MytasksService"
		//graphDb.shutdown();
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
