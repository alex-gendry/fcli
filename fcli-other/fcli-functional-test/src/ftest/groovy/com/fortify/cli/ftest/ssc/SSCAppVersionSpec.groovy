package com.fortify.cli.ftest.ssc;

import static com.fortify.cli.ftest._common.spec.FcliSessionType.SSC

import com.fortify.cli.ftest._common.Fcli
import com.fortify.cli.ftest._common.Fcli.UnexpectedFcliResultException
import com.fortify.cli.ftest._common.spec.FcliBaseSpec
import com.fortify.cli.ftest._common.spec.FcliSession
import com.fortify.cli.ftest._common.spec.Prefix
import com.fortify.cli.ftest.ssc._common.SSCAppVersionSupplier

import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Stepwise

@Prefix("ssc.appversion") @FcliSession(SSC) @Stepwise
class SSCAppVersionSpec extends FcliBaseSpec {
    @Shared @AutoCleanup SSCAppVersionSupplier versionSupplier = new SSCAppVersionSupplier()
    
    def "list"() {
        def args = "ssc appversion list"
        when:
            def result = Fcli.run(args)
        then:
            verifyAll(result.stdout) {
                size()>=2
                it[0].replace(" ","").equals("IdApplicationnameNameIssuetemplatenameCreatedby");
            }
    }
    
    def "get.byName"() {
        def args = "ssc appversion get " + versionSupplier.version.appName + ":" + versionSupplier.version.versionName
        when:
            def result = Fcli.run(args)
        then:
            verifyAll(result.stdout) {
                it[1].equals("id: " + versionSupplier.version.get("id"));
            }
    }
    
    def "get.byId"() {
        def args = "ssc appversion get " + versionSupplier.version.get("id")
        when:
            def result = Fcli.run(args)
        then:
            verifyAll(result.stdout) {
                it[4].equals("  name: \"" + versionSupplier.version.appName + "\"");
                it[9].equals("name: \"" + versionSupplier.version.versionName + "\"");
            }
    }
    
    def "updateName"() {
        def args = "ssc appversion update " + versionSupplier.version.get("id") + " --name updatedVersionName --description updated1 -o table=name,description"
        when:
            def result = Fcli.run(args)
        then:
            verifyAll(result.stdout) {
                size()==2
                it[1].contains("updatedVersionName");
            }
    }
    
    def "updateNameWithMatchingAppName"() {
        def args = "ssc appversion update " + versionSupplier.version.get("id") + " --name " + versionSupplier.version.appName + ":updatedVersionName2 --description updated2 -o table=name,description"
        when:
            def result = Fcli.run(args)
        then:
            verifyAll(result.stdout) {
                size()==2
                it[1].contains("updatedVersionName2");
            }
    }
    
    def "updateNameWithMatchingAppNameAndCustomDelimiter"() {
        def args = "ssc appversion update " + versionSupplier.version.get("id") + " --name " + versionSupplier.version.appName + "|updatedVersionName3 --description updated2 --delim | -o table=name,description"
        when:
            def result = Fcli.run(args)
        then:
            verifyAll(result.stdout) {
                size()==2
                it[1].contains("updatedVersionName3");
                !it[1].contains("|")
            }
    }
    
    def "updateNameWithNonMatchingAppName"() {
        def args = "ssc appversion update " + versionSupplier.version.get("id") + " --name nonExistingAppversion123:updatedVersionName3 --description updated3"
        when:
            def result = Fcli.run(args)
        then:
            def e = thrown(UnexpectedFcliResultException)
            verifyAll(e.result.stderr) {
                it[0].startsWith("java.lang.IllegalArgumentException: --name option must contain either a plain name or ${versionSupplier.version.appName}:<new name>, current: nonExistingAppversion123:updatedVersionName3")
            }
    }
}