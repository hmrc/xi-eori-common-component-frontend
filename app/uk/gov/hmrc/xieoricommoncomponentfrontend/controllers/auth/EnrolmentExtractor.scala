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

import uk.gov.hmrc.xieoricommoncomponentfrontend.domain.{LoggedInUserWithEnrolments, Nino, Utr}

trait EnrolmentExtractor {

  private def identifierFor(
    enrolmentKey: String,
    identifierName: String,
    loggedInUser: LoggedInUserWithEnrolments
  ): Option[String] =
    loggedInUser.enrolments
      .getEnrolment(enrolmentKey)
      .flatMap(
        enrolment =>
          enrolment
            .getIdentifier(identifierName)
            .map(identifier => identifier.value)
      )

  def enrolledCtUtr(loggedInUser: LoggedInUserWithEnrolments): Option[Utr] =
    identifierFor("IR-CT", "UTR", loggedInUser).map(Utr)

  def enrolledSaUtr(loggedInUser: LoggedInUserWithEnrolments): Option[Utr] =
    identifierFor("IR-SA", "UTR", loggedInUser).map(Utr)

  def enrolledNino(loggedInUser: LoggedInUserWithEnrolments): Option[Nino] =
    identifierFor("HMRC-NI", "NINO", loggedInUser).map(Nino)

}
