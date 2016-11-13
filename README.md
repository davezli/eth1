# janest-eth1

Algorithmic Trading Bot used for Jane St's eth1 competition.

## Algorithm
Used forecasting based off current trades as historical data to predict trends. Valued stocks as the arithmetic mean of high buy offer and lowest sell offer. Flipped a random amount of stocks based off those trends.

## Results
Peaked at #5 in the competition, finished in top 10.

## Possible Improvements
* Prevention of bulk buying of stocks by using weighted probability
* Implement smarter selling (instead of waiting for sell offers to go through, cancel and sell at the highest buying price if it meant more short-term liquidity)
* Take advantage of bundled stocks by hard-coding monitors for them
