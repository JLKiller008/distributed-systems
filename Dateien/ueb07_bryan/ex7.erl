-module(ex7). 
-export([convert/2, maxitem/1, diff/3]). 

convert(Dist, cm) ->
{inch, Dist / 2.54};
convert(Dist, inch) ->
{cm, Dist * 2.54}.



maxitem([]) ->
io:format("Recursion end~n"),
0;
maxitem([X|Xs]) ->
io:format("Head: ~p, Rest: ~p~n", [X, Xs]),
maxitem(X, Xs).

maxitem(Highest, []) ->
io:format("Recursion end~n"),
Highest;
maxitem(Highest, [X|Xs]) ->
io:format("Highest: ~p, Head: ~p, Rest: ~p~n", [Highest, X, Xs]),
maxitem(max(Highest, X), Xs).




diff(F, X, H) ->
(F(X + H) - F(X - H)) / (2 * H).



