package pl.kempa.saska.bdd;

import org.junit.runner.RunWith;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/test/resources/features",
    plugin = {"pretty", "html:target/cucumber/bagbasics"},
    extraGlue = "pl.kempa.saska.bdd")
public class CucumberE2ETest {
}
