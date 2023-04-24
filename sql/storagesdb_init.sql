\c storagesDB
CREATE TABLE public.tblstorage (
    id SERIAL PRIMARY KEY,
    storagetype VARCHAR (10) NOT NULL,
    bucketname VARCHAR (100) NOT NULL Unique,
    path VARCHAR (100)
);

