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
import uk.gov.hmrc.xieoricommoncomponentfrontend.connectors.{EnrolmentStoreProxyConnector, SubscriptionDisplayConnector}
import uk.gov.hmrc.xieoricommoncomponentfrontend.domain.{EnrolmentResponse, GroupId}
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.{ErrorResponse, SubscriptionDisplayResponseDetail}
import uk.gov.hmrc.xieoricommoncomponentfrontend.util.EoriUtils
import uk.gov.hmrc.xieoricommoncomponentfrontend.viewmodels.ConfirmDetailsViewModel

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
        val subscriptionDisplayResponse = subscriptionDisplayConnector.call(buildQueryParameters(gbEori))
        subscriptionDisplayResponse.map {
          case Right(resp) =>
            sessionCache.saveSubscriptionDisplay(resp).map(
              successfulWrite =>
                if (!successfulWrite)
                  logger.debug("SubscriptionDisplay SUB09 details retrieved successfully and saved into cache")
            )
          case Left(_) => logger.debug("SubscriptionDisplay SUB09 call failed and details are not saved into cache")
        }
        subscriptionDisplayResponse
    }

}
