package org.beryx.recommend

import com.google.gson.Gson

import static org.beryx.recommend.Constants.ISINs

class Recommender {
    static int updateRecoFile(File file) {
        def oldRecos = getRecommendationsFromFile(file)
        def currentRecos = getCurrentRecommendations()
        int count = 0
        def gson = new Gson()
        currentRecos.each {isin, reco ->
            def oldDate = oldRecos[isin]?.date
            if(!oldDate || reco.date.after(oldDate)) {
                file.append(gson.toJson(reco) + '\n')
                count++
            }
            println "count = $count; oldDate($isin) = $oldDate; reco.date = $reco.date"
        }
        println "$file updated: $count rows appended."
        return count
    }

    static Map<String, Recommendation> getRecommendationsFromFile(File file) {
        Map<String, Recommendation> recoMap = [:]
        if(!file.file) {
            println "$file does not exist"
        } else {
            def gson = new Gson()
            file.eachLine {line ->
                if(line.trim()) {
                    def reco = gson.fromJson(line, Recommendation)
                    recoMap[reco.isin] = reco
                    println "### read reco: $reco"
                }
            }
        }
        println "${recoMap.size()} entries read from $file"
        return recoMap
    }

    static Map<String, Recommendation> getCurrentRecommendations() {
//        create()
//        def tagsoupParser = new Parser()
//        def slurper = new XmlSlurper(tagsoupParser)



        Map<String, Recommendation> recoMap = [:]
        int failureCount = 0
        for(isin in ISINs) {
            sleep(2000)
            try {
                recoMap[isin] = Recommendation.ofIsin(isin)
                println recoMap[isin]
            } catch (Exception e) {
                println "########## Cannot retrieve recommendations for isin $isin: $e !!!"
                failureCount++
            }
        }

        println "retrieved recommendations for ${ISINs.size() - failureCount} out of ${ISINs.size()}"
        return recoMap
    }
}
