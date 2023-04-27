\c mp3ResourceDB
CREATE TABLE public.tblmp3resourceinfo (
    id SERIAL PRIMARY KEY,
    resourceid integer NOT NULL Unique,
    filesize bigint,
    storageid integer NOT NULL
);

