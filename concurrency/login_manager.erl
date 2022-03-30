-module(login_manager).

-export([start/0,loop/1,create_account/2,close_account/2,login/2,logout/1,online/0]).

% interface functions
start() ->
    register(?MODULE,spawn(fun() -> loop(#{}) end)).

% main process
loop(Map) -> 
    receive
        {create_account, User, Passwd, Pid} -> 
            case maps:find(User,Map) of
                error -> 
                    Pid ! {?MODULE, ok},
                    io:format("New user ~p\n",[User]),
                    loop(maps:put(User,{Passwd,false},Map));
                _ -> 
                    Pid ! {?MODULE, user_exists},
                    io:format("User ~p already exists\n", [User]),
                    loop(Map)
            end;

        {close_account, User, Passwd, Pid} -> 
            case maps:find(User,Map) of
                {ok, {Passwd,_}} ->
                    Pid ! {?MODULE, ok},
                    io:format("Closing user ~p\n",[User]),
                    loop(maps:remove(User,Map));
                _ ->
                    Pid ! {?MODULE, invalid},
                    io:format("User ~p invalid or password invalid\n",[User]),
                    loop(Map)
            end;
        {login, User, Passwd , Pid} ->
            case maps:find(User,Map) of
                {ok, {Passwd,false}} ->
                    Pid ! {?MODULE, ok},
                    io:format("logged in\n"),
                    loop(maps:update(User,{Passwd,true},Map));
            
                _ ->
                    Pid ! {?MODULE, invalid},
                    io:format("Wrong User, wrong password or already logged in\n"),
                    loop(Map)
            end;
        {logout, User , Pid} ->
            case maps:find(User, Map) of
                {ok, {Pass,true}} ->
                    Pid ! {?MODULE,ok},
                    io:format("logged out\n"),
                    loop(maps:update(User,{Pass,false},Map));
                _ -> 
                    Pid ! {?MODULE, invalid},
                    loop(Map)
            end;
        {online , Pid} ->
            Pid ! {?MODULE, maps:filter(fun(_,{_,LOG}) -> LOG end, Map)},
            loop(Map)    
    end.


% rpc(Req) -> 
%     ?MODULE ! {Req, self()},
%     receive {?MODULE, Res} -> Res end.

% create_account(User,Passwd) ->
%     rpc({create_account, User, Passwd}).
    
create_account(User,Passwd) ->
    ?MODULE ! {create_account, User, Passwd, self()},
    receive {?MODULE, Res} -> Res end.

close_account(User,Passwd) ->
    ?MODULE ! {close_account, User, Passwd, self()},
    receive {?MODULE, Res} -> Res end.

login(User,Passwd) ->
    ?MODULE ! {login, User, Passwd, self()},
    receive {?MODULE, Res} -> Res end.

logout(User) ->
    ?MODULE ! {logout, User, self()},
    receive {?MODULE, Res} -> Res end.

online() ->
    ?MODULE ! {online, self()},
    receive {?MODULE, Res} -> Res end.