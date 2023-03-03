package contracts.songcontroller

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "When a POST request with a SongInfoDTO is made, the created song's resourceId is returned"
    request {
        method POST()
        url("/api/songs")
        body(
                resourceId: 111,
                title: "test-title",
                artist: "test-artist",
                album: "test-album",
                length: "0:15",
                releaseDate: "2023-03-01"
        )
        headers {
            contentType(applicationJson())
        }
    }
    response {
        status 200
        body(
                id: 111
        )
        headers {
            contentType(applicationJson())
        }
    }
}