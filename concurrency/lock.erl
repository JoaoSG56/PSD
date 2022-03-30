-module(lock).
-export([create/0,reading/1,reading/2,writing/1,aquire/1,released/0,release/0]).

create() ->
    register(?MODULE,spawn(fun() -> released() end)).

released() ->
    receive
        {aquire, read , Pid} -> Pid ! {ok, ?MODULE}, reading([Pid])
    end.

reading([]) -> released();
reading(Locks) -> 
    receive
        {aquire, read, Pid} -> Pid ! {ok, ?MODULE}, reading([Pid | Locks]);
        {release, Pid} -> Pid ! {ok, ?MODULE}, reading(Locks -- [Pid]);
        {aquire, write, Pid} -> reading(Locks, Pid)
    end.
reading([], Writer) ->
    Writer ! {ok, ?MODULE}, writing(Writer);
reading(Locks,Writer) ->
    receive
        {release, Pid} -> Pid ! {ok, ?MODULE} , reading(Locks -- [Pid],Writer)
    end.

writing(Pid) ->
    receive
        {release, Pid} -> Pid ! {ok, ?MODULE} , released()
    end.

aquire(Mode) ->
    ?MODULE ! {aquire,Mode, self()},
    receive {Res, ?MODULE} -> Res end.
release() ->
    ?MODULE ! {release, self()},
    receive {Res, ?MODULE} -> Res end.