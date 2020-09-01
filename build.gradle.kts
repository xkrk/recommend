import org.beryx.recommend.*

plugins {
    id("org.ajoberstar.git-publish") version "2.1.3"
}

gitPublish {
    repoUri.set("https://github.com/beryx/badass-jlink-plugin.git")
    branch.set("master")
    contents {
        from("data/reco.txt")
    }
}


task("updateRecommendations") {
    doFirst {
        val count = Recommender.updateRecoFile(file("data/reco.txt"))
        if(count > 0) {

        }
    }
}
