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

package uk.gov.hmrc.xieoricommoncomponentfrontend.connectors

import play.api.Logger
import uk.gov.hmrc.http.{HttpClient, _}
import uk.gov.hmrc.xieoricommoncomponentfrontend.config.AppConfig
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.{
  ErrorResponse,
  ServiceUnavailableResponse,
  SubscriptionDisplayResponseDetail
}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

@Singleton
class SubscriptionDisplayConnector @Inject() (http: HttpClient, appConfig: AppConfig)(implicit ec: ExecutionContext) {

  private val logger = Logger(this.getClass)
  private val url    = s"${appConfig.subscriptionDisplayBaseUrl}/${appConfig.subscriptionDisplayServiceContext}"

  def call(
    sub09Request: Seq[(String, String)]
  )(implicit hc: HeaderCarrier): Future[Either[ErrorResponse, SubscriptionDisplayResponseDetail]] = {

    // $COVERAGE-OFF$Loggers
    logger.debug(s"Call: $url , body: $sub09Request, and hc: $hc")
    // $COVERAGE-ON

    http.GET[SubscriptionDisplayResponseDetail](url, sub09Request) map { resp =>
      // $COVERAGE-OFF$Loggers
      logger.debug("SubscriptionDisplay SUB09 details retrieved successfully")
      // $COVERAGE-ON

      Right(resp)
    }
  } recover {
    case NonFatal(e) =>
      logger.error(s"SubscriptionDisplay SUB09 failed. url: $url, error: $e")
      Left(ServiceUnavailableResponse)
  }

}
