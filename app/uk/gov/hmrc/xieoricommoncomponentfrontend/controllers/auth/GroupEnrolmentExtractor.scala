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

  def getEori(user: LoggedInUserWithEnrolments)(implicit headerCarrier: HeaderCarrier): Future[Option[String]] =
    sessionCache.eori flatMap {
      case Some(value) => Future.successful(Some(value))
      case None =>
        existingEoriForUser(user.enrolments.enrolments) match {
          case Some(eori) =>
            sessionCache.saveEori(Eori(eori))
            Future.successful(Some(eori))
          case None => existingEoriForGroup(user)
        }
    }

  def existingEoriForGroup(
    user: LoggedInUserWithEnrolments
  )(implicit headerCarrier: HeaderCarrier): Future[Option[String]] =
    for {
      groupEnrolment <- groupIdEnrolments(GroupId(user.groupId))
      mayBeEori = groupEnrolment.find(_.eori.exists(_.nonEmpty)).flatMap(enrolment => enrolment.eori)
    } yield {
      if (mayBeEori.isDefined) sessionCache.saveEori(Eori(mayBeEori.get))
      mayBeEori
    }

  def existingEoriForUser(userEnrolments: Set[Enrolment]): Option[String] = {
    val userEnrolmentWithEori = userEnrolments.find(_.identifiers.exists(_.key == EoriIdentifier))
    userEnrolmentWithEori.flatMap(enrolment => enrolment.getIdentifier(EoriIdentifier).map(_.value))
  }

}
