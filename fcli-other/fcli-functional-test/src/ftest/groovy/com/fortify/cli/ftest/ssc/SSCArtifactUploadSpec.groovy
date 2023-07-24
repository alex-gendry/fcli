package com.fortify.cli.ftest.ssc;

import static com.fortify.cli.ftest._common.spec.FcliSessionType.SSC

import com.fortify.cli.ftest._common.Fcli
import com.fortify.cli.ftest._common.spec.FcliBaseSpec
import com.fortify.cli.ftest._common.spec.FcliSession
import com.fortify.cli.ftest._common.spec.Prefix
import com.fortify.cli.ftest._common.spec.TestResource
import com.fortify.cli.ftest.ssc._common.SSCAppVersion

import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Stepwise

@Prefix("ssc.artifact") @FcliSession(SSC) @Stepwise
class SSCArtifactUploadSpec extends FcliBaseSpec {
    @Shared @AutoCleanup SSCAppVersion version = new SSCAppVersion().create()
    @Shared @TestResource("runtime/shared/EightBall-22.1.0.fpr") String fpr
    @Shared String uploadVariableName = version.fcliVariableName+"_artifact"
    @Shared String uploadVariableRef = "::$uploadVariableName::"
    
    def "upload"() {
        def args = "ssc artifact upload $fpr --appversion "+ 
            "${version.variableRef} --store $uploadVariableName"
        when:
            def result = Fcli.run(args)
        then:
            verifyAll(result.stdout) {
                it[0] =~ /Id\s+Scan types\s+Last scan date\s+Upload date\s+Status/
                it[1] =~ /^\s*\d+.*/
            }
    }
    
    def "wait-for"() {
        // Depending on externalmetadata versions in FPR and on SSC, approval
        // may be required
        def args = "ssc artifact wait-for $uploadVariableRef -i 2s --until PROCESS_COMPLETE|REQUIRE_AUTH"
        when:
            def result = Fcli.run(args)
        then:
            verifyAll(result.stdout) {
                //
            }
    }
    
    def "list"() {
        def args = "ssc artifact list --appversion ${version.variableRef}"
        when:
            def result = Fcli.run(args)
        then:
            verifyAll(result.stdout) {
                //
            }
    }
    
    def "get"() {
        def args = "ssc artifact get $uploadVariableRef"
        when:
            def result = Fcli.run(args)
        then:
            verifyAll(result.stdout) {
                //
            }
    }
    
    
}