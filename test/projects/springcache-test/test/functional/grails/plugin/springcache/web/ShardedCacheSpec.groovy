package grails.plugin.springcache.web

import net.sf.ehcache.Ehcache
import musicstore.pages.*
import spock.lang.*
import musicstore.pages.ProfileEditPage

@Issue("http://jira.codehaus.org/browse/GRAILSPLUGINS-2167")
@Stepwise
@Unroll
class ShardedCacheSpec extends AbstractContentCachingSpec {

	void setupSpec() {
		setUpUser "blackbeard", "Edward Teach"
		setUpUser "roundhouse", "Chuck Norris"
	}

	void cleanup() {
		logout()
	}

	void cleanupSpec() {
		tearDownUsers()
	}

	void "#username only sees their own profile"() {
		given: "a user is logged in"
		to LoginPage
		loginAs username

		when: "they visit the profile page"
		to ProfilePage

		then: "they see their own profile"
		title == expectedTitle

		and: "their profile is cached separately"
		cacheFor(username).statistics.cacheMisses == 1

		where:
		username     | expectedTitle
		"blackbeard" | "Profile: Edward Teach"
		"roundhouse" | "Profile: Chuck Norris"
	}

	void "when re-visiting the profile page #username sees their own profile delivered from cache"() {
		given: "a user is logged in"
		to LoginPage
		loginAs username

		when: "they visit the profile page"
		to ProfilePage

		then: "they see their own profile"
		title == expectedTitle

		and: "their profile is cached separately"
		cacheFor(username).statistics.cacheHits == 1

		where:
		username     | expectedTitle
		"blackbeard" | "Profile: Edward Teach"
		"roundhouse" | "Profile: Chuck Norris"
	}

	void "when a user updates their profile the cache is flushed"() {
		given: "a user is logged in"
		to LoginPage
		loginAs "blackbeard"

		and: "they visit the edit profile page"
		to ProfileEditPage

		when: "they update their profile"
		profile.name = "Edward Thatch"
		profile.find("button").click(ProfilePage)

		then: "they see their updated profile"
		at ProfilePage
		title == "Profile: Edward Thatch"

		and: "the profile page is not served from the cache"
		cacheFor("blackbeard").statistics.cacheMisses == old(cacheFor("blackbeard").statistics.cacheMisses) + 1
	}

	private Ehcache cacheFor(String username) {
		return springcacheService.getOrCreateCache("profileCache-$username")
	}

}
