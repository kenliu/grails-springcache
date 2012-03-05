/*
 * Copyright 2010 Grails Plugin Collective
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.plugin.springcache.web

import grails.test.mixin.web.ControllerUnitTestMixin
import grails.test.mixin.*
import spock.lang.*

@TestMixin(ControllerUnitTestMixin)
@Mock(TestController)
class ContentCacheParametersSpec extends Specification {

	@Unroll
	void "controller is #expectedController.simpleName and action is #expectedActionName when controllerName is '#controllerName' and actionName is '#actionName'"() {
		given:
		webRequest.controllerName = controllerName
		webRequest.actionName = actionName

		when:
		def cacheParameters = new ContentCacheParameters(webRequest)

		then:
		cacheParameters.controller?.clazz == expectedController
		cacheParameters.action?.name == expectedActionName

		where:
		controllerName | actionName     | expectedController | expectedActionName
		null           | null           | null               | null
		"test"         | "index"        | TestController     | "index"
		"test"         | "list"         | TestController     | "list"
		"test"         | null           | TestController     | "index"
		"test"         | "blah"         | TestController     | null
		"test"         | "methodAction" | TestController     | 'methodAction' // http://jira.grails.org/browse/GPSPRINGCACHE-47
	}
}

class TestController {

	def index = {}
	def list = {}

	def methodAction() {}

}
