package eternal

import grails.plugin.springcache.annotations.Cacheable

@Cacheable('eternalCache')
class EternalCacheController {

	def index = {
		[message: 'THIS SHOULD BE CACHED FOREVER']
	}

}
