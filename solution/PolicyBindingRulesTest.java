package org.acme.insurance.monitoring;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.acme.insurance.PolicyBinding;
import org.drools.core.time.SessionPseudoClock;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.logger.KieRuntimeLogger;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.EntryPoint;
import org.kie.api.runtime.rule.FactHandle;

/**
 * BLD Workshop Sample JUnit Test Class for Complex Event Processing Rules Using
 * the pseudo clock to simulate the passing of time.
 */

public class PolicyBindingRulesTest {

    static KieBase kbase;
    static KieSession ksession;
    static KieRuntimeLogger klogger;
    static SessionPseudoClock clock;

    @BeforeClass
    public static void setupKsession() {
        try {
            // load up the knowledge base
            ksession = readKnowledgeBase();
            clock = ksession.getSessionClock();
            klogger = KieServices.Factory.get().getLoggers().newFileLogger(ksession, "src/test/java/org/acme/insurance/monitoring/policyBindingRulesTest");

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static KieSession readKnowledgeBase() throws Exception {
        
        KieServices ks = KieServices.Factory.get();
        
        KieContainer kContainer = ks.getKieClasspathContainer();
        KieSession kSession = kContainer.newKieSession();
        
        return kSession;
    }
    
    @AfterClass
    public static void closeKsession() {
        try {
            // closing resources
            if(klogger != null)
                klogger.close();
            if(ksession != null)
                ksession.dispose();

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Test
    public void exceedThresholdTest() {

        // Create policy binding list
        ArrayList<PolicyBinding> policyBindingList = new ArrayList<PolicyBinding>();
        int pbvalue = 500;

        while (policyBindingList.size() < 12) {
            policyBindingList.add(new PolicyBinding(pbvalue, new Date()));
            pbvalue += 50;
        }

        // ------------------------------------------- LAB HINT:
        // to use a separate stream for inserts
        EntryPoint pbStream = ksession.getEntryPoint( "policy_binding_stream" );

        // /create fact handle list
        // insert objects into working memory while advancing the clock
        ArrayList<FactHandle> factHandleList = new ArrayList<FactHandle>();
        for (int i = 0; i < policyBindingList.size(); i++) {
            factHandleList.add(pbStream.insert(policyBindingList.get(i)));
            clock.advanceTime(2, TimeUnit.SECONDS);
            System.out.println("Advanced by 2 seconds");
        }

        clock.advanceTime(7, TimeUnit.SECONDS);

        StringBuilder sBuilder = new StringBuilder();
        ksession.setGlobal("policyAverage", sBuilder);

        ksession.fireAllRules();

        // remove facts
        for (int i = 0; i < factHandleList.size(); i++) {
            pbStream.delete(factHandleList.get(i));
        }

        String result = sBuilder.substring(0, 45);
        System.out
                .println("exceedThresholdTest Result: " + sBuilder.toString());
        assertEquals("Average Price is over 710",
                "Increase Reserves.  Average Price is over 710", result);
    }

    @Test
    public void withinThresholdTest() {

        // Create policy binding list
        ArrayList<PolicyBinding> policyBindingList = new ArrayList<PolicyBinding>();
        int pbvalue = 500;
        while (policyBindingList.size() < 12) {
            policyBindingList.add(new PolicyBinding(pbvalue, new Date()));
            pbvalue += 10;
        }

        // ------------------------------------------- LAB HINT:
        // to use a separate stream for inserts
        EntryPoint pbStream = ksession.getEntryPoint( "policy_binding_stream" );

        // /create fact handle list
        // insert objects into working memory while advancing the clock
        ArrayList<FactHandle> factHandleList = new ArrayList<FactHandle>();
        for (int i = 0; i < policyBindingList.size(); i++) {
            factHandleList.add(pbStream.insert(policyBindingList.get(i)));
            clock.advanceTime(2, TimeUnit.SECONDS);
            System.out.println("Advanced by 2 seconds");
        }
        clock.advanceTime(7, TimeUnit.SECONDS);

        StringBuilder sBuilder = new StringBuilder();
        ksession.setGlobal("policyAverage", sBuilder);

        ksession.fireAllRules();

        // remove facts
        for (int i = 0; i < factHandleList.size(); i++) {
            pbStream.delete(factHandleList.get(i));
        }

        String result = sBuilder.substring(0, 34);
        System.out
                .println("withinThresholdTest Result: " + sBuilder.toString());
        assertEquals("Average Price is under 710",
                "Average Price under the threashold", result);

    }
}
