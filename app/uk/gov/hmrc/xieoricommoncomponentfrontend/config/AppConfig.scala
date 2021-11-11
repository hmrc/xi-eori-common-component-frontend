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
import play.api.i18n.Messages

import scala.concurrent.duration.Duration

@Singleton
class AppConfig @Inject() (config: Configuration, servicesConfig: ServicesConfig) {

  val welshLanguageSupportEnabled: Boolean =
    config.getOptional[Boolean]("features.welsh-language-support").getOrElse(false)

  lazy val loginContinueUrl: String = config.get[String]("external-urls.loginContinue")
  lazy val loginUrl: String         = config.get[String]("external-urls.login")
  lazy val signOutUrl: String       = config.get[String]("external-urls.signOut")
  lazy val xiVatRegisterUrl: String = config.get[String]("external-urls.xiVatRegisterUrl")
  lazy val appName: String          = config.get[String]("appName")

  val ttl: Duration = Duration.create(config.get[String]("ecc-frontend-cache.ttl"))

  val enrolmentStoreProxyBaseUrl: String = servicesConfig.baseUrl("enrolment-store-proxy")

  val enrolmentStoreProxyServiceContext: String =
    config.get[String]("microservice.services.enrolment-store-proxy.context")

  //handle subscription service
  val xiEoriCommonComponentBaseUrl: String = servicesConfig.baseUrl("xi-eori-common-component")

  val xiEoriCommonComponentContext: String =
    config.get[String]("microservice.services.xi-eori-common-component.context")

  private def languageKey(implicit messages: Messages) = messages.lang.language match {
    case "cy" => "cy"
    case _    => "en"
  }

  def callCharges()(implicit messages: Messages): String =
    config.get[String](s"external-urls.call-charges-$languageKey")

  def getServiceUrl(proxyServiceName: String): String = {
    val baseUrl = servicesConfig.baseUrl("xi-eori-common-component")
    val serviceContext =
      config.get[String](s"microservice.services.xi-eori-common-component.$proxyServiceName.context")
    s"$baseUrl/$serviceContext"
  }

}
