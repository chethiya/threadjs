chars = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz'
cId = ''

setId = (id) ->
 cId = "[#{id}]"

randomString = (length = 32) ->
 string = ''

 for i in [0...length]
  randomNumber = Math.floor Math.random() * chars.length
  string += chars.substring randomNumber, randomNumber + 1

 string

logUser = (msg) ->
 console.log "#{cId}[LOG]", msg

logError = (err, data) ->
 console.error "#{cId}[ERROR]#{err}", data

debug = (data) ->
 console.log "#{cId}[DEBUG]", data

exports.randomString = randomString
exports.logUser = logUser
exports.logError = logError
exports.debug = debug
exports.setId = setId

