import groovy.json.*
import static com.xlson.groovycsv.CsvParser.parseCsv
import groovyx.net.http.*
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import static groovyx.net.http.Method.POST
import static groovyx.net.http.ContentType.JSON
import groovyx.net.http.ContentType
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import groovyx.net.http.RESTClient
import org.apache.http.HttpRequest
import org.apache.http.HttpRequestInterceptor
import org.apache.http.protocol.HttpContext

import org.apache.commons.io.FileUtils

import LiferayCommerceContext


@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7.1')
@Grab('com.xlson.groovycsv:groovycsv:1.0')
@Grab('org.jsoup:jsoup:1.6.1')
@Grab(group='commons-io', module='commons-io', version='2.8.0')
@GrabConfig(systemClassLoader = true)

//Source System Variables
def sourceURL = ""
def sourceUsername = ""
def sourcePassword = ""
def sourceLiferayCommerceContext = new LiferayCommerceContext(sourceURL, sourceUsername, sourcePassword)


//Destination System Variables
def destinationURL = ""
def destinationUsername = ""
def destinationPassword = ""
def destinationLiferayCommerceContext = new LiferayCommerceContext(destinationURL, destinationUsername, destinationPassword)

//Migrate Specification Groups
migrateSpecificationGroups(sourceLiferayCommerceContext, destinationLiferayCommerceContext);

//Migrate Specifications
migrateSpecifications(sourceLiferayCommerceContext, destinationLiferayCommerceContext);


def migrateSpecificationGroups(LiferayCommerceContext sourceLiferayCommerceContext, LiferayCommerceContext destinationLiferayCommerceContext){

    def sourceSpecificationGroups = sourceLiferayCommerceContext.specificationGroups
    def count = 0
    sourceSpecificationGroups.each{
        def specificationGroupKey = it.value.key
        def specificationGroupTitle = it.value.title.en_US
        
        destinationLiferayCommerceContext.createSpecificationGroup(specificationGroupKey, specificationGroupTitle)
        count++
    }
    println "Migrated  $count Specification Groups"
}

def migrateSpecifications(LiferayCommerceContext sourceLiferayCommerceContext, LiferayCommerceContext destinationLiferayCommerceContext){

    def sourceSpecifications = sourceLiferayCommerceContext.specifications
    println "Migrating " + sourceSpecifications.size + " Specifications"

    def count = 0
    sourceSpecifications.each{
        def specificationKey = it.value.key
        def specificationTitle = it.value.title.en_US
        def specificationFacetable = it.value.facetable
        def specificationGroup = destinationLiferayCommerceContext.specificationGroups[it.value.optionCategory.key]

        destinationLiferayCommerceContext.createSpecification(specificationKey, specificationTitle, specificationFacetable, specificationGroup)
        count++
    }
    println "Migrated  $count Specifications"
}