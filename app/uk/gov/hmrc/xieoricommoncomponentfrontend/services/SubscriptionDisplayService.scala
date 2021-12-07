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

package uk.gov.hmrc.xieoricommoncomponentfrontend.services

import play.api.Logger
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.xieoricommoncomponentfrontend.cache.SessionCache
import uk.gov.hmrc.xieoricommoncomponentfrontend.connectors.SubscriptionDisplayConnector
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.{
  ErrorResponse,
  ServiceUnavailableResponse,
  SubscriptionDisplayResponseDetail
}
import uk.gov.hmrc.xieoricommoncomponentfrontend.util.EoriUtils

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubscriptionDisplayService @Inject() (
  subscriptionDisplayConnector: SubscriptionDisplayConnector,
  utils: EoriUtils,
  sessionCache: SessionCache
)(implicit ec: ExecutionContext) {
  private val logger = Logger(this.getClass)

  def buildQueryParameters(eori: String) =
    List("EORI" -> eori, "regime" -> "CDS", "acknowledgementReference" -> utils.generateUUIDAsString)

  def getSubscriptionDisplay(
    gbEori: String
  )(implicit hc: HeaderCarrier): Future[Either[ErrorResponse, SubscriptionDisplayResponseDetail]] =
    sessionCache.subscriptionDisplay flatMap {
      case Some(value) if value.EORINo.isDefined => Future.successful(Right(value))
      case _ =>
        subscriptionDisplayConnector.call(buildQueryParameters(gbEori)).flatMap {
          case Right(resp) =>
            sessionCache.saveSubscriptionDisplay(resp).map(_ => Right(resp))
          case Left(_) =>
            logger.debug("SubscriptionDisplay SUB09 call failed and details are not saved into cache")
            Future.successful(Left(ServiceUnavailableResponse))

        }

    }

}
