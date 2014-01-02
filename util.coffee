crypto = require 'crypto'

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

padInt = (d) -> "#{if d < 10 then '0' else ''}#{d}"
getStoreDate = (d) -> "#{d.getYear()+1900}#{padInt d.getMonth()+1}#{padInt d.getDate()}"

getDateFromStore = (d) ->
 yy = parseInt d.substr 0, 4
 mm = parseInt d.substr 4, 2
 dd = parseInt d.substr 6, 2
 return new Date yy, mm-1, dd

exports.randomString = randomString
exports.logUser = logUser
exports.logError = logError
exports.debug = debug
exports.setId = setId

