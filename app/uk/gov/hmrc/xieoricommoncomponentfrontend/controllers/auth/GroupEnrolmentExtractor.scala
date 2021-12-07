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

package uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.auth

import uk.gov.hmrc.auth.core.Enrolment
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.xieoricommoncomponentfrontend.cache.SessionCache
import uk.gov.hmrc.xieoricommoncomponentfrontend.domain._
import uk.gov.hmrc.xieoricommoncomponentfrontend.services.EnrolmentStoreProxyService

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GroupEnrolmentExtractor @Inject() (
  enrolmentStoreProxyService: EnrolmentStoreProxyService,
  sessionCache: SessionCache
)(implicit val ec: ExecutionContext) {
  private val EoriIdentifier: String = "EORINumber"

  def groupIdEnrolments(groupId: GroupId)(implicit hc: HeaderCarrier): Future[List[EnrolmentResponse]] =
    enrolmentStoreProxyService.enrolmentsForGroup(groupId)

  def getEori(user: LoggedInUserWithEnrolments)(implicit headerCarrier: HeaderCarrier): Future[Option[Eori]] =
    sessionCache.eori flatMap {
      case Some(value) => Future.successful(Some(Eori(value)))
      case None =>
        existingEoriForUser(user.enrolments.enrolments) match {
          case Some(eori) => sessionCache.saveEori(Eori(eori)).map(_ => Some(Eori(eori)))
          case None       => existingEoriForGroup(user)
        }
    }

  def existingEoriForGroup(
    user: LoggedInUserWithEnrolments
  )(implicit headerCarrier: HeaderCarrier): Future[Option[Eori]] =
    groupIdEnrolments(GroupId(user.groupId)).flatMap {
      groupEnrolment =>
        groupEnrolment.find(_.eori.exists(_.nonEmpty)).flatMap(enrolment => enrolment.eori) match {
          case Some(value) => sessionCache.saveEori(Eori(value)).map(_ => Some(Eori(value)))
          case _           => Future.successful(None)
        }
    }

  def existingEoriForUser(userEnrolments: Set[Enrolment]): Option[String] = {
    val userEnrolmentWithEori = userEnrolments.find(_.identifiers.exists(_.key == EoriIdentifier))
    userEnrolmentWithEori.flatMap(enrolment => enrolment.getIdentifier(EoriIdentifier).map(_.value))
  }

}
