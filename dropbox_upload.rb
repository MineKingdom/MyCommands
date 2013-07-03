require 'dropbox_sdk'

API_KEY = ARGV[0]
API_SECRET = ARGV[1]
ACCESS_KEY = ARGV[2]
ACCESS_SECRET = ARGV[3]
LOCAL = ARGV[4]
REMOTE = ARGV[5]

ACCESS_TYPE = :app_folder

session = DropboxSession.new(API_KEY, API_SECRET)
session.set_access_token(ACCESS_KEY, ACCESS_SECRET)

client = DropboxClient.new(session, ACCESS_TYPE)

file = open(LOCAL)
response = client.put_file(REMOTE, file)
puts response.inspect
puts "Uploaded " + LOCAL + " to " + response["path"]
