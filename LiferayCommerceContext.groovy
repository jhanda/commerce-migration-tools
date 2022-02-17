import groovy.json.*
import groovy.sql.Sql
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

import LiferayCommerceContext

@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7.1')
@Grab('com.xlson.groovycsv:groovycsv:1.0')
@Grab('org.jsoup:jsoup:1.6.1')
@GrabConfig(systemClassLoader = true)

class LiferayCommerceContext {                                    
    

    
    LiferayCommerceContext(liferayBaseUrl, liferayUsername, liferayPassword){
        this.liferayBaseUrl = liferayBaseUrl
        this.liferayUsername = liferayUsername
        this.liferayPassword = liferayPassword
        
        specificationGroups = loadSpecificationGroups(liferayBaseUrl, liferayUsername, liferayPassword)
        specifications = loadSpecifications(liferayBaseUrl, liferayUsername, liferayPassword)
    }

    def loadSpecificationGroups(liferayBaseUrl, liferayUsername, liferayPassword){
            
            def specificationGroups = [:]
            def http = new HTTPBuilder(liferayBaseUrl)

            http.client.addRequestInterceptor(new HttpRequestInterceptor() {
                void process(HttpRequest httpRequest, HttpContext httpContext) {
                    httpRequest.addHeader('Authorization', 'Basic ' + "$liferayUsername:$liferayPassword".bytes.encodeBase64().toString())
                }
            })

            def response = http.request(Method.GET, ContentType.TEXT){req ->
                uri.path = "o/headless-commerce-admin-catalog/v1.0/optionCategories/"
                headers.Accept = 'application/json'
                headers.'User-Agent' = 'Apache HTTPClient'

                response.success = {resp, read ->
                    def slurper = new JsonSlurper()
                    def results = slurper.parseText(read.text)
                    for (def item: results.items){
                        //println(item.id + " -- " + item.key + " -- " + item.title.en_US);
                        specificationGroups[item.key] = item
                    }
                }
            }

        return specificationGroups;
    }

    def loadSpecifications(liferayBaseUrl, liferayUsername, liferayPassword){
            
            def specifications = [:]
            def http = new HTTPBuilder(liferayBaseUrl)

            http.client.addRequestInterceptor(new HttpRequestInterceptor() {
                void process(HttpRequest httpRequest, HttpContext httpContext) {
                    httpRequest.addHeader('Authorization', 'Basic ' + "$liferayUsername:$liferayPassword".bytes.encodeBase64().toString())
                }
            })

            def response = http.request(Method.GET, ContentType.TEXT){req ->
                uri.path = "o/headless-commerce-admin-catalog/v1.0/specifications/?pageSize=100"
                headers.Accept = 'application/json'
                headers.'User-Agent' = 'Apache HTTPClient'

                response.success = {resp, read ->
                    def slurper = new JsonSlurper()
                    def results = slurper.parseText(read.text)
                    for (def item: results.items){
                        //println(item.id + " -- " + item.key + " -- " + item.title.en_US);
                        specifications[item.key] = item
                    }
                }
            }

        return specifications;
    }

    def createSpecificationGroup(specificationGroupKey, specificationGroupTitle){
        
        def optionCategoriesUrl = liferayBaseUrl + "o/headless-commerce-admin-catalog/v1.0/optionCategories"
        def http = new HTTPBuilder(optionCategoriesUrl)

        http.request( POST, JSON ) { req ->
	
	        headers.'Authorization' = 'Basic ' + "$liferayUsername:$liferayPassword".bytes.encodeBase64().toString()

	        body = [
                "description": [
                    "en_US": ""
                ],
                "key": specificationGroupKey,
                "title": [
                    "en_US": specificationGroupTitle
                ]
	        ]
	
            response.success = { resp, json ->
    		    //println "Successfully created $json.key"
                specificationGroups[json.key] = json
                return json
  	        }

            response.failure = { resp ->
		        println "request failed with status ${resp.status}, response body was [${resp.entity.content.text}]"
    	        //throw new Exception("Exception processing $name.  Received response status: $resp.status")
  	        }
        }
    }

    def createSpecification(specificationKey, specificationTitle, specificationFacetable, specificationGroup){       

        def specificationsUrl = liferayBaseUrl + "o/headless-commerce-admin-catalog/v1.0/specifications"
        def http = new HTTPBuilder(specificationsUrl)

        http.request( POST, JSON ) { req ->
	
	        headers.'Authorization' = 'Basic ' + "$liferayUsername:$liferayPassword".bytes.encodeBase64().toString()

	        body = [
                "description": [
                    "en_US": ""
                ],
                "facetable": specificationFacetable,
                "key": specificationKey,
                "title": [
                    "en_US": specificationTitle
                ],
                "optionCategory": [
                    "id": specificationGroup.id,
                    "key": specificationGroup.key,
                    "title": [
                        "en_US": specificationGroup.title.en_US
                    ]
                ]
	        ]
	
            response.success = { resp, json ->
    		    // println "Successfully created $json.key"
                specifications[json.key] = json
                return json
  	        }

            response.failure = { resp ->
		        println "request failed with status ${resp.status}, response body was [${resp.entity.content.text}]"
    	        //throw new Exception("Exception processing $name.  Received response status: $resp.status")
  	        }
        }
    }

    
    String liferayBaseUrl 
    String liferayUsername
    String liferayPassword
    def specificationGroups
    def specifications
}