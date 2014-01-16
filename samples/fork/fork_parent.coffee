ForkThread = (require './../../fork_thread').ForkThread
util = (require './../../util')
util.setId '[Fork Parent]'
logUser = util.logUser

parent = new ForkThread parent: true, program: "./fork_child.coffee", params: []

parent.getRecord = (data, callback) ->
 logUser "Data receved"
 logUser data
 callback null, {id: 0, vendor: "pamona", amount: 1231.32, date: "12312013"}

 for i in [0...10]
  parent.send 'onRecord', {id: 0, vendor: "pamona", amount: 1231.32, date: "12312013"}, ->

parent.onStarted (err, data) ->
 console.log "Child process started : ", err

console.log "Starting the parent process"
