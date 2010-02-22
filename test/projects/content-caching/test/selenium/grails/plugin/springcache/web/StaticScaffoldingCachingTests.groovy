package grails.plugin.springcache.web

import grails.plugin.springcache.web.AbstractContentCachingTestCase
import musicstore.Album
import musicstore.Artist
import musicstore.pages.AlbumCreatePage
import musicstore.pages.AlbumListPage
import musicstore.pages.AlbumShowPage
import net.sf.ehcache.Ehcache

class StaticScaffoldingCachingTests extends AbstractContentCachingTestCase {

	Ehcache albumControllerCache

	void setUp() {
		super.setUp()
		albumControllerCache = springcacheCacheManager.getEhcache("AlbumControllerCache")
		assert albumControllerCache, "Cache named AlbumControllerCache not found in $springcacheCacheManager.cacheNames"
	}

	void testOpeningListPageWithEmptyCache() {
		def page = AlbumListPage.open()
		assertEquals "Album List", page.title

		assertEquals 0, albumControllerCache.statistics.cacheHits
		assertEquals 1, albumControllerCache.statistics.cacheMisses
	}

	void testReloadingListPageHitsCache() {
		def page = AlbumListPage.open()
		assertEquals "Album List", page.title

		page = page.refresh()
		assertEquals "Album List", page.title

		assertEquals 1, albumControllerCache.statistics.cacheHits
		assertEquals 1, albumControllerCache.statistics.objectCount
	}

	void testCachedResponseIsDecoratedBySitemesh() {
		def page = AlbumListPage.open()
		assertTrue page.sitemeshDecorated

		page = page.refresh()
		assertTrue page.sitemeshDecorated

		assertEquals 1, albumControllerCache.statistics.cacheHits
	}

	void testSaveFlushesCache() {
		def listPage = AlbumListPage.open()
		assertEquals 0, listPage.rowCount

		def createPage = AlbumCreatePage.open()
		createPage."artist.name" = "Edward Sharpe & the Magnetic Zeros"
		createPage.name = "Up From Below"
		createPage.year = "2009"
		createPage.save()

		assertEquals "Album failed to save", 1, Album.count()

		listPage = AlbumListPage.open()
		assertEquals "Album list page is still displayed cached content", 1, listPage.rowCount

		assertEquals 0, albumControllerCache.statistics.cacheHits
		assertEquals 3, albumControllerCache.statistics.cacheMisses // 2 misses on list page, 1 on show
		assertEquals 2, albumControllerCache.statistics.objectCount // show and list pages cached
	}

	void testDifferentShowPagesCachedSeparately() {
		def artist = Artist.build(name: "Metric")
		def album1 = Album.build(artist: artist, name: "Fantasies", year: "2009")
		def album2 = Album.build(artist: artist, name: "Live It Out", year: "2005")

		def showPage1 = AlbumShowPage.open(album1.id)
		assertEquals album1.name, showPage1.Name

		def showPage2 = AlbumShowPage.open(album2.id)
		assertEquals album2.name, showPage2.Name

		assertEquals 0, albumControllerCache.statistics.cacheHits
		assertEquals 2, albumControllerCache.statistics.cacheMisses
		assertEquals 2, albumControllerCache.statistics.objectCount
	}

}
