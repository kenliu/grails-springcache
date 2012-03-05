package grails.plugin.springcache.web

import net.sf.ehcache.Ehcache
import org.codehaus.groovy.grails.commons.ApplicationHolder
import spock.lang.*

@Issue('http://jira.grails.org/browse/GPSPRINGCACHE-43')
class EternalCacheSpec extends AbstractContentCachingSpec {

	@Shared Ehcache eternalCache = ApplicationHolder.application.mainContext.eternalCache

	void setup() {
		eternalCache.statisticsEnabled = true
	}
	
	void 'eternal cache is eternal'() {
		when:
		go '/eternalCache/index'
		
		then:
		eternalCache.statistics.objectCount == 1
		def key = eternalCache.keys.first()
		def entry = eternalCache.get(key)
		entry.isEternal()
		entry.timeToLive == 0
	}
	
}
