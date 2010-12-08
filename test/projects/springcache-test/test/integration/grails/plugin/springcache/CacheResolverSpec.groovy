package grails.plugin.springcache

import grails.plugin.spock.IntegrationSpec
import net.sf.ehcache.Statistics
import pirates.Pirate
import static pirates.Context.*
import spock.lang.*

@Stepwise
class CacheResolverSpec extends IntegrationSpec {

	def piracyService
	@AutoCleanup("removalAll") @Shared def springcacheCacheManager

	def setupSpec() {
		Pirate.withNewSession { session ->
			Pirate.build(name: "Guybrush Threepwood", context: Fictional)
			Pirate.build(name: "Jack Sparrow", context: Fictional)
			Pirate.build(name: "Blackbeard", context: Historical)
			Pirate.build(name: "Calico Jack", context: Historical)
			session.flush()
		}
	}

	def cleanupSpec() {
		Pirate.withNewSession { session ->
			Pirate.list()*.delete()
			session.flush()
		}
	}

	def "a method annotation can declare a custom cache resolver"() {
		given: piracyService.currentContext = Fictional

		expect:
		piracyService.listPiratesForContext() == ["Guybrush Threepwood", "Jack Sparrow"]

		and:
		springcacheCacheManager.cacheExists("pirateCache-Fictional")

		and:
		getStats("pirateCache-Fictional").cacheMisses == 1
	}

	def "subsequent calls may use the same cache"() {
		expect:
		piracyService.listPiratesForContext() == ["Guybrush Threepwood", "Jack Sparrow"]

		and:
		getStats("pirateCache-Fictional").cacheHits == 1
	}

	def "the resolver can cause a different cache to be used"() {
		given: piracyService.currentContext = Historical

		expect:
		piracyService.listPiratesForContext() == ["Blackbeard", "Calico Jack"]

		and:
		springcacheCacheManager.cacheExists("pirateCache-Historical")

		and:
		getStats("pirateCache-Historical").cacheMisses == 1
	}

	def "caches can be re-used by the resolver"() {
		given: piracyService.currentContext = Fictional

		expect:
		piracyService.listPiratesForContext() == ["Guybrush Threepwood", "Jack Sparrow"]

		and:
		getStats("pirateCache-Fictional").cacheHits == 2
	}

	private Statistics getStats(String name) {
		springcacheCacheManager.getEhcache(name).statistics
	}

}
