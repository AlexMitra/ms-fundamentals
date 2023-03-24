\c songsDB
CREATE TABLE public.tblSongInfo (
    id SERIAL PRIMARY KEY,
    resourceid integer NOT NULL Unique,
    title VARCHAR (50),
    artist VARCHAR (50),
    album VARCHAR (50),
    length VARCHAR (10),
    releaseDate VARCHAR (10)
);