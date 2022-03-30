-module(myqueue).
-export([create/0,enqueue/2,dequeue/1]).

%create() -> [].

%enqueue(Queue,Item) ->
%	Queue ++ [Item].


%dequeue([]) -> empty;
%dequeue([H|T]) -> {T,H}.



create() -> { [], [] }.

enqueue({ In, Out }, Elem) -> { [Elem | In], Out }.

dequeue({ [], [] }) -> empty;
dequeue({ In, [] }) -> dequeue({ [], lists:reverse(In) });
dequeue({ In, [X|XS] }) -> { {In, XS}, X }.
