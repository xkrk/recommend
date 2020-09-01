package org.beryx.recommend

import groovy.transform.Canonical

import java.text.SimpleDateFormat

@Canonical
class Recommendation {
    String isin
    double price
    double buyPercent
    int buyCount
    int neutralCount
    int sellCount
    Date date

    static Recommendation ofIsin(String isin) {
        def url = "https://finanzen.handelsblatt.com/analysen_check.htn?suchbegriff=$isin"
        def text = new URL(url).text
        fromUrlText(text, isin)
    }

    static Recommendation fromUrlText(String text, String isin) {
        def price = getPriceRecommendation(text, isin)
        def buyPercent = getBuyRecommendationPercent(text, isin)
        def buyCount = getBuyRecommendationCount(text, isin)
        def neutralCount = getNeutralRecommendationCount(text, isin)
        def sellCount = getSellRecommendationCount(text, isin)
        def date = getRecommendationDate(text, isin)
        new Recommendation(isin: isin, price: price, buyPercent: buyPercent,
                buyCount: buyCount, neutralCount: neutralCount, sellCount: sellCount,
                date: date)
    }

    private static double getPriceRecommendation(String text, String isin) {
        def m = text =~ '(?s).*Gewichtete Empfehlung <span class="vhb-vwd-worth">([0-9,]+) &euro;</span>.*'
        if(!m.matches()) throw new IllegalArgumentException("Cannot get price recommendation for isin $isin")
        String sVal = m[0][1]
        sVal = sVal.replace(',', '.')
        double value = Double.parseDouble(sVal)
        return value
    }

    private static int getBuyRecommendationPercent(String text, String isin) {
        def m = text =~ '(?s).*Gewichtete Empfehlung <span class="vhb-vwd-worth">([0-9,]+) %</span>.*'
        if(!m.matches()) throw new IllegalArgumentException("Cannot get buy recommendation for isin $isin")
        String sVal = m[0][1]
        sVal = sVal.replace(',', '.')
        double value = Double.parseDouble(sVal)
        return value
    }

    private static int getRecommendationCount(String classSuffix, String type, String text, String isin) {
        def pattern = '(?s).*<span class="vhb-vwd-bar-text vhb-vwd-' + classSuffix + '">' + type + ': ([0-9]+)</span>.*'
        def m = text =~ pattern
        if(!m.matches()) throw new IllegalArgumentException("Cannot get $type recommendation count for isin $isin")
        String sVal = m[0][1]
        double value = Integer.parseInt(sVal)
        return value
    }

    private static int getBuyRecommendationCount(String text, String isin) {
        getRecommendationCount('up', 'Buy', text, isin)
    }

    private static int getNeutralRecommendationCount(String text, String isin) {
        getRecommendationCount('new', 'Neutral', text, isin)
    }

    private static int getSellRecommendationCount(String text, String isin) {
        getRecommendationCount('down', 'Sell', text, isin)
    }

    private static Date getRecommendationDate(String text, String isin) {

//        Veröffentlichung der Original-Studie: 07.08.2020 / 08:40 / MESZ
        def m = text =~ '(?s).*ffentlichung der Original-Studie: ([^\\v]+).*'
        if(!m.matches()) throw new IllegalArgumentException("Cannot get recommendation date for isin $isin")
        String sVal = m[0][1]

        Date recoDate = null
        def tokens = sVal.split('/')
        if(tokens.length == 3) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy / HH:mm / zzz");
            try {
                recoDate = sdf.parse(sVal.trim());
            } catch (Exception e) {
                println "$isin: cannot parse as 3-token: $sVal"
            }
        }
        if(!recoDate && tokens.length >= 2) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy / HH:mm");
            try {
                recoDate = sdf.parse(sVal.trim());
            } catch (Exception e) {
                println "$isin: cannot parse as 2-token: $sVal"
            }
        }
        if(!recoDate) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
            recoDate = sdf.parse(tokens[0].trim());
        }
        return recoDate
    }
}
