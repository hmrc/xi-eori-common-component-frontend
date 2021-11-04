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

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.xieoricommoncomponentfrontend.cache.SessionCache
import uk.gov.hmrc.xieoricommoncomponentfrontend.connectors.EnrolmentStoreProxyConnector
import uk.gov.hmrc.xieoricommoncomponentfrontend.domain.{EnrolmentResponse, GroupId}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EnrolmentStoreProxyService @Inject() (
  enrolmentStoreProxyConnector: EnrolmentStoreProxyConnector,
  sessionCache: SessionCache
)(implicit ec: ExecutionContext) {

  private val activatedState = "Activated"

  def enrolmentsForGroup(groupId: GroupId)(implicit hc: HeaderCarrier): Future[List[EnrolmentResponse]] =
    sessionCache.groupEnrolment flatMap {
      case Some(value) => Future.successful(value)
      case None =>
        for {
          groupEnrolments <- enrolmentStoreProxyConnector
            .getEnrolmentByGroupId(groupId.id)
            .map(_.enrolments)
            .map(enrolment => enrolment.filter(x => x.state == activatedState))
          _ <- sessionCache.saveGroupEnrolment(groupEnrolments)
        } yield groupEnrolments
    }

}
