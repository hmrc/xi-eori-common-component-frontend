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

package controllers.auth

import uk.gov.hmrc.auth.core.{Enrolment, Enrolments}
import uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.auth.EnrolmentExtractor
import uk.gov.hmrc.xieoricommoncomponentfrontend.domain._
import util.ControllerSpec

class EnrolmentExtractorSpec extends ControllerSpec {

  private val eori = Eori("GB123456789012")
  private val utr  = Utr("1111111111K")
  private val nino = Nino("NINO")

  private def loggedInUser(enrolments: Set[Enrolment]) =
    LoggedInUserWithEnrolments(None, None, Enrolments(enrolments), None, None)

  val enrolmentExtractor = new EnrolmentExtractor {}

  "Enrolment Extractor" should {

    "return Self Assessment UTR" when {

      "user has correct enrolment" in {

        val selfAssessmentEnrolment = Enrolment("IR-SA").withIdentifier("UTR", utr.id)

        val result = enrolmentExtractor.enrolledSaUtr(loggedInUser(Set(selfAssessmentEnrolment)))

        result shouldBe Some(utr)
      }
    }

    "doesn't return Self Assessment UTR" when {

      "user doesn't have correct enrolment" in {

        enrolmentExtractor.enrolledSaUtr(loggedInUser(Set.empty)) shouldBe None
      }
    }

    "return Corporation Tax UTR" when {

      "user has correct enrolment" in {

        val corporationTaxEnrolment = Enrolment("IR-CT").withIdentifier("UTR", utr.id)

        val result = enrolmentExtractor.enrolledCtUtr(loggedInUser(Set(corporationTaxEnrolment)))

        result shouldBe Some(utr)
      }
    }

    "doesn't return Corporation Tax UTR" when {

      "user doesn't have correct enrolment" in {

        enrolmentExtractor.enrolledCtUtr(loggedInUser(Set.empty)) shouldBe None
      }
    }

    "return Nino" when {

      "user has correct enrolment" in {

        val ninoEnrolment = Enrolment("HMRC-NI").withIdentifier("NINO", nino.id)

        val result = enrolmentExtractor.enrolledNino(loggedInUser(Set(ninoEnrolment)))

        result shouldBe Some(nino)
      }
    }

    "doesn't return Nino" when {

      "user doesn't have correct enrolment" in {

        enrolmentExtractor.enrolledNino(loggedInUser(Set.empty)) shouldBe None
      }
    }

    "return existing EORI for user and/or group" when {

      "user has enrolment with an EORI" in {

        val userEnrolments                           = Set(Enrolment("HMRC-TEST-ORG").withIdentifier("EORINumber", eori.id))
        val groupEnrolments: List[EnrolmentResponse] = List.empty

        val result = enrolmentExtractor.existingEoriForUserOrGroup(userEnrolments, groupEnrolments)

        result shouldBe Some(ExistingEori(eori.id, "HMRC-TEST-ORG"))
      }

      "user's group has enrolment with an EORI" in {

        val userEnrolments = Set(Enrolment("HMRC-NI").withIdentifier("NINO", nino.id))
        val groupEnrolments: List[EnrolmentResponse] =
          List(EnrolmentResponse("HMRC-GROUP-ORG", "Active", List(KeyValue("EORINumber", eori.id))))

        val result = enrolmentExtractor.existingEoriForUserOrGroup(userEnrolments, groupEnrolments)

        result shouldBe Some(ExistingEori(eori.id, "HMRC-GROUP-ORG"))
      }

      "user has no enrolment with EORI nor group enrolment with EORI" in {

        val userEnrolments = Set(Enrolment("HMRC-NI").withIdentifier("NINO", nino.id))
        val groupEnrolments =
          List(EnrolmentResponse("HMRC-GROUP-ORG", "Active", List(KeyValue("OtherIdentifierKey", "SomeValue"))))

        val result = enrolmentExtractor.existingEoriForUserOrGroup(userEnrolments, groupEnrolments)

        result shouldBe None
      }

      "user enrolments or group enrolments" in {

        val userEnrolments: Set[Enrolment]           = Set.empty
        val groupEnrolments: List[EnrolmentResponse] = List.empty

        val result = enrolmentExtractor.existingEoriForUserOrGroup(userEnrolments, groupEnrolments)

        result shouldBe None
      }
    }
  }
}
