class BootStrap {

    def init = { servletContext ->
		
		System.out.println "in BootStrap"
		java.net.URL.metaClass.queryAsMap = {
			if(!delegate.query) return [:]
		  
			delegate.query.split('&').inject([:]) {map, kv ->
			  def (key, value) = kv.split('=').toList()
			  if(value != null) {
				map[key] = URLDecoder.decode(value)
			  }
			  return map
			}
		  }
		
    }
    def destroy = {
    }
}
