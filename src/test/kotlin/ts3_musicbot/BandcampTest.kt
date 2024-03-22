package ts3_musicbot

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import ts3_musicbot.services.Bandcamp
import ts3_musicbot.util.Link
import kotlin.test.Test
import kotlin.test.assertEquals

class BandcampTest {
    private val bandcamp = Bandcamp()
    @Test
    fun testGettingTrack() {
        runBlocking(Dispatchers.IO) {
            //Bandcamp link to track: Side B from Echo Chamber (2023 Year End Mixtape)
            val testLink = Link("https://visceraandvapor.bandcamp.com/track/side-b-3")
            val track = bandcamp.fetchTrack(testLink)
            assertEquals("\uD835\uDC3A\uD835\uDC42\uD835\uDC45\uD835\uDC38", track.artists.artists[0].name.name)
            assertEquals("Side B", track.title.name)
        }
    }

    @Test
    fun testGettingAlbum() {
        runBlocking(Dispatchers.IO) {
            //Bandcamp link to album: Echo Chamber (2023 Year End Mixtape)
            val testLink = Link("https://visceraandvapor.bandcamp.com/album/echo-chamber-2023-year-end-mixtape")
            val album = bandcamp.fetchAlbum(testLink)
            assertEquals("Echo Chamber (2023 Year End Mixtape)", album.name.name)
            assertEquals(2, album.tracks.size)
        }
    }

    @Test
    fun testGettingAlbum2() {
        runBlocking(Dispatchers.IO) {
            //Bandcamp link to album: Molded By Broken Hands
            val testLink = Link("https://greyskiesfallen.bandcamp.com/album/molded-by-broken-hands")
            val album = bandcamp.fetchAlbum(testLink)
            assertEquals("Molded By Broken Hands", album.name.name)
            assertEquals(7, album.tracks.size)
        }
    }

    @Test
    fun testGettingAlbum3() {
        runBlocking(Dispatchers.IO) {
            //Bandcamp link to album: War Of Being
            val testLink = Link("https://kscopemusic.bandcamp.com/album/war-of-being")
            val album = bandcamp.fetchAlbum(testLink)
            assertEquals("War Of Being", album.name.name)
            assertEquals("TesseracT", album.artists.artists.first().name.name)
            assertEquals(9, album.tracks.size)
        }
    }

    @Test
    fun testGettingArtist() {
        runBlocking(Dispatchers.IO) {
            //Bandcamp link to artist: dirty river
            val testLink = Link("https://dirty-river.bandcamp.com")
            val artist = bandcamp.fetchArtist(testLink)
            assertEquals("dirty river", artist.name.name)
            assert(artist.albums.albums.first { it.name.name == "dirty river" }.tracks.size == 11)
        }
    }
}