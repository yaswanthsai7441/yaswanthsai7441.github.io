import requests as rq


def getStockData(stockSymbol, startDate, endDate):
    baseUrl = 'https://marketdata.websol.barchart.com/getHistory.json'
    apiKey = '92cf55f827a639c549214a6462bcd484'
    params = {'apikey': apiKey, 'symbol': stockSymbol,
              'type': 'daily', 'startDate': startDate,
              'endDate': endDate}
    # we filtered all data such as symbols or date
    dataResponse = rq.get(baseUrl, params)
    # to make sure our url will be worked properly:
    print(dataResponse.url)
    return dataResponse.json()


# this function parse the JSON data and extract the information into the 5 categories
def decodeJsonResponse(jsonData):
    openPrices = []
    closePrices = []
    highs = []
    lows = []
    volumes = []
    results = jsonData.get('results')
    for result in results:
        openPrices.append(result.get('open'))
        closePrices.append(result.get('close'))
        highs.append(result.get('high'))
        lows.append(result.get('low'))
        volumes.append(result.get('volume'))
    return openPrices, closePrices, highs, lows, volumes


# calculation differences in values and some factors
# and the differences is between the next day and the previous day
# we can take the close price off today and find the differences between that and the close price of the yesterday
def calculateDifferences(inputArray):
    differences = []
    for i in range(len(inputArray) - 1):  # i put len -1 owing to avoiding out of bound exception
        difference = inputArray[i + 1] - inputArray[i]
        if difference >= 0:
            differences.append(1)
        else:
            differences.append(0)
    return differences


# it will be give us the open price of the next day and the close price of the current day
# it is extremely integral method because it going to train our model
# if the open price the next day is higher than the close price of the previous day store [1, 0] otherwise [0, 1]


def priceDifferenceCalculation(openPrice, closePrice):
    priceDifferences = []
    for i in range(len(openPrice) - 1):
        difference = openPrice[i + 1] - closePrice[i]
        if difference >= 0:
            priceDifferences.append([1, 0])
        else:
            priceDifferences.append([0, 1])
    return priceDifferences


# everything is brought together by guessing in this method
# guessing all of below differences assigning our labels
# and then it is going to build up our dataset as being an array
# where we are taking the index of open differences, close differences,
# high, low, volume and passing that into its own array
def dataSubset(stockSymbol, startDate, endDate):
    jsonData = getStockData(stockSymbol, startDate, endDate)
    openPrices, closePrices, highs, lows, volumes = decodeJsonResponse(jsonData)

    openDifferences = calculateDifferences(openPrices)
    closeDifferences = calculateDifferences(closePrices)
    highsDifferences = calculateDifferences(highs)
    lowsDifferences = calculateDifferences(lows)
    volumesDifferences = calculateDifferences(volumes)

    labels = priceDifferenceCalculation(openPrices, closePrices)

    finalFormattingData = []
    for i in range(len(openDifferences)):
        finalFormattingData.append([openDifferences[i], closeDifferences[i], highsDifferences[i], lowsDifferences[i],
                                    volumesDifferences[i]])
    return finalFormattingData, labels


print(dataSubset('AAPL', '20181201', '20190521'))
# jsonStockData = getStockData('AAPL', '20160101', '20190424')
# with print(getStockData('AAPL', '20160101', '20190424')) you can get all data as json text
# in this part we have converted data from JSON to decoded variables
# so we can predict our specific stock through decoded variable
# for instance we can select just openPrices as a reference and make prediction via the filtered option. print(o)...
# o, c, h, l, v = decodeJsonResponse(jsonStockData)
# print(h)
