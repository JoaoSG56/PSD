-module(chatv3).
-export([start_server/1]).

start_server(Port) ->
  {ok, LSock} = gen_tcp:listen(Port, [binary, {active, once}, {packet, line},
                                      {reuseaddr, true}]),


  Manager = spawn(fun() -> manager(#{}) end),
  
  spawn(fun() -> acceptor(LSock, Manager) end),
  ok.

acceptor(LSock, Manager) ->
  {ok, Sock} = gen_tcp:accept(LSock),
  spawn(fun() -> acceptor(LSock, Manager) end),
  Manager ! {enter,default,self()},
  receive
    {ok,Pid} -> 
      user(Sock,Pid);
    _ -> ok
  end.
%  user(Sock, Manager).

manager(Rooms) ->
  receive
    {enter,Name,Pid} ->
      io:format("user to enter ~p\n",[Name]),
      case maps:find(Name,Rooms) of
        {ok,Value} -> 
          io:format("entrar"),
          Value ! {enter,Pid},
          Pid ! {ok,Value},
          manager(Rooms);
        _ ->
          io:format("creating room\n"),
          NewRoom = spawn(fun() -> room(self(),[]) end),
          R = maps:put(Name,NewRoom,Rooms),
          NewRoom ! {enter,Pid},
          Pid ! {ok,NewRoom},
          manager(R)
      end
  end.

room(RoomManager,Pids) ->
  receive
    {enter, Pid} ->
      io:format("user entered~n", []),
      room(RoomManager,[Pid | Pids]);
    {line, {Pid,"\\enter " ++ Room}} ->
      io:format("user switching room to ~p~n",[Room]),
      RoomManager ! {enter,Room,Pid},
      room(RoomManager,Pids --[Pid]);
    {line, Data} = Msg ->
      io:format("received ~p~n", [Data]),
      [Pid ! Msg || Pid <- Pids],
      room(RoomManager,Pids);
    {leave, Pid} ->
      io:format("user left~n", []),
      room(RoomManager,Pids -- [Pid]);
    {switch, Name, Pid} ->
      io:format("user switching\n"),
      RoomManager ! {enter,Name,Pid},
      room(RoomManager,Pids -- [Pid])
  end.

user(Sock, Room) ->
  Self = self(),
  receive
    {line, {Self, Data}} ->
      inet:setopts(Sock, [{active, once}]),
      gen_tcp:send(Sock, Data),
      user(Sock, Room);
    {line, {_, Data}} ->
      gen_tcp:send(Sock, Data),
      user(Sock, Room);
    {tcp, _, Data} ->
      Room ! {line, {Self, Data}},
      user(Sock, Room);
    {tcp_closed, _} ->
      Room ! {leave, self()};
    {tcp_error, _, _} ->
      Room ! {leave, self()}
  end.

