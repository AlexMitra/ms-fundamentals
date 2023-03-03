Feature: End to End tests for song-service API
  Scenario: client is able to call POST /api/songs and save SongInfoDTO
    When the client calls /api/songs with SongInfoDTO in body
    Then the client receives status code of 200
    And the client receives id of saved SongInfoDTO
    Then the client is able to call GET /api/songs/resourceId with same id
    And the client receives saved SongInfoDTO
