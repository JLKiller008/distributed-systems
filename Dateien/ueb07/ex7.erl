-module(ex7).
-export([convert/2, maxitem/1, diff/3]).

convert(Amount, inch) ->
    {cm, Amount * 2.54};
convert(Amount, cm) ->
    {inch, Amount / 2.54}.

maxitem([]) ->
    0;
maxitem([Head | Tail]) ->
    io:format("maxitem(~p)~n", [[Head | Tail]]),
    maxitem(Tail, Head).

maxitem([], Maximum) ->
    io:format("maxitem([], ~p) -> ~p~n", [Maximum, Maximum]),
    Maximum;
maxitem([Head | Tail], Maximum) ->
    NewMaximum = erlang:max(Head, Maximum),
    io:format("maxitem([~p | ~p], ~p) -> maxitem(~p, ~p)~n",
              [Head, Tail, Maximum, Tail, NewMaximum]),
    maxitem(Tail, NewMaximum).

diff(F, X, H) ->
    (F(X + H) - F(X - H)) / (2 * H).

% Anonymous function given in exercise:
% F = fun(X) -> 2 * X * X * X - 12 * X + 3 end.