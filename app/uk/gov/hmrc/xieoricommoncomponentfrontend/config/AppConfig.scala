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

package uk.gov.hmrc.xieoricommoncomponentfrontend.config

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class AppConfig @Inject() (config: Configuration, servicesConfig: ServicesConfig) {

  val welshLanguageSupportEnabled: Boolean =
    config.getOptional[Boolean]("features.welsh-language-support").getOrElse(false)

  lazy val loginContinueUrl: String = config.get[String]("external-urls.loginContinue")
  lazy val loginUrl: String         = config.get[String]("external-urls.login")
  lazy val signOutUrl: String       = config.get[String]("external-urls.signOut")
  lazy val appName: String          = config.get[String]("appName")

  val enrolmentStoreProxyBaseUrl: String = servicesConfig.baseUrl("enrolment-store-proxy")

  val enrolmentStoreProxyServiceContext: String =
    config.get[String]("microservice.services.enrolment-store-proxy.context")

  //handle subscription service
  val subscriptionDisplayBaseUrl: String = servicesConfig.baseUrl("subscription-display")

  val subscriptionDisplayServiceContext: String =
    config.get[String]("microservice.services.subscription-display.context")


}
