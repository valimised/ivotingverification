ivoteverification
=================

Android based vote verification application for Estonian i-voting system

The intention behind this repository is to make source code of the official i-vote verification application for Estonian internet-voting system available for public review.

The repository is not used for active development, but will be kept up to date, so the code that can be found here is the code that is used for election. As the voting system used for legally binding elections must strictly follow the legislation, the actual development of Estonian i-voting system and i-vote verification application is supervised by National Electoral Committee (NEC) and Internet Voting Committee (www.vvk.ee). The software vendor of i-vote verification application is AS Finestmedia (www.finestmedia.ee).

Additional information on the source code can be found on the NEC website: http://www.vvk.ee/valijale/e-haaletamine/

Those, who are not familiar with Estonian language may refer to the following website, which contains subset of the information in English: http://www.vvk.ee/voting-methods-in-estonia/

Notes for testers and developers:

The verification application downloads its configuration in JSON format from configuration server. The URL of the config server is stored as a single line in config.txt file in res/raw/config.txt 

config.txt:
https://  config-server URL  /config.json

The verification application uses HTTPS protocol and verifies the server certificate. All certificates required to verify server certificate have to be stored in BouncyCastle format keystore in res/raw/mytruststore.bks
