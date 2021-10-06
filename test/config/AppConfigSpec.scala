/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package config

import org.mockito.Mockito
import org.mockito.Mockito.spy
import org.scalatest.BeforeAndAfterEach
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import util.ControllerSpec

class AppConfigSpec extends ControllerSpec with BeforeAndAfterEach {

  private val mockConfig: Configuration = spy(config)
  private val mockServiceConfig         = mock[ServicesConfig]

  override def beforeEach() {
    super.beforeEach()
    Mockito.reset(mockConfig, mockServiceConfig)
  }

  "AppConfig" should {

    "have enrolmentStoreProxyBaseUrl defined" in {
      appConfig.enrolmentStoreProxyBaseUrl shouldBe "http://localhost:6754"
    }

    "have enrolmentStoreProxyServiceContext defined" in {
      appConfig.enrolmentStoreProxyServiceContext shouldBe "enrolment-store-proxy"
    }

  }

}
