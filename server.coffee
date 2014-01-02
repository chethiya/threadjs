JavaThread = (require './../java_thread').JavaThread
util = (require './../util')
util.setId 'Server'
logUser = util.logUser

server = new JavaThread server: true, spawn: false
server.getRecord = (data, callback) ->
 logUser "Data receved"
 logUser data
 callback null, {id: 0, vendor: "pamona", amount: 1231.32, date: "12312013"}

server.onStarted (err, data) ->
 console.log "Server started : ", err

console.log "Starting the server"
