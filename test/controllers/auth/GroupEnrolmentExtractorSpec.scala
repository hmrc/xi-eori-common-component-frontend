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

import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.auth.core.{Enrolment, Enrolments}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.xieoricommoncomponentfrontend.cache.SessionCache
import uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.auth.{EnrolmentExtractor, GroupEnrolmentExtractor}
import uk.gov.hmrc.xieoricommoncomponentfrontend.domain._
import uk.gov.hmrc.xieoricommoncomponentfrontend.services.EnrolmentStoreProxyService
import util.BaseSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class GroupEnrolmentExtractorSpec extends BaseSpec with BeforeAndAfterEach {

  private val enrolmentStoreProxyService = mock[EnrolmentStoreProxyService]
  private val sessionCache               = mock[SessionCache]

  private val enrolmentResponse =
    EnrolmentResponse("HMRC-CUS-ORG", "ACTIVATED", List(KeyValue("EORINumber", "GB123456463324")))

  private val hc = HeaderCarrier()

  private val groupEnrolmentExtractor = new GroupEnrolmentExtractor(enrolmentStoreProxyService, sessionCache)

  private val nino = Nino("NINO")

  private def loggedInUser(enrolments: Set[Enrolment]) =
    LoggedInUserWithEnrolments(None, None, Enrolments(enrolments), None, Some("groupId"))

  private val eori = Eori("GB123456789012")

  override protected def afterEach(): Unit = {
    reset(enrolmentStoreProxyService)
    reset(sessionCache)
    super.afterEach()
  }

  "GroupEnrolmentExtractor" should {

    "return all group enrolments" when {

      "groupId has enrolments" in {

        when(enrolmentStoreProxyService.enrolmentsForGroup(any())(any()))
          .thenReturn(Future.successful(List(enrolmentResponse)))

        val result = await(groupEnrolmentExtractor.groupIdEnrolments("groupId")(hc))

        result shouldBe List(enrolmentResponse)
      }

      "user's group has enrolment with an EORI" in {
        val response = enrolmentResponse.copy(identifiers = List(KeyValue("EORINumber", eori.id)))
        when(enrolmentStoreProxyService.enrolmentsForGroup(any())(any()))
          .thenReturn(Future.successful(List(response)))
        val enrolments = Set(Enrolment("HMRC-CUS-ORG").withIdentifier("EORINumber", eori.id))
        val result     = await(groupEnrolmentExtractor.existingEoriForGroup(loggedInUser(enrolments))(hc))

        result shouldBe Some(eori.id)
      }

    }

    "fetch eori from Session Cache if eori is already saved in cache" in {
      when(
        sessionCache
          .eori(any())
      ).thenReturn(Future.successful(Some(eori.id)))
      val enrolments = Set(Enrolment("HMRC-CUS-ORG").withIdentifier("EORINumber", eori.id))
      await(groupEnrolmentExtractor.getEori(loggedInUser(enrolments))(hc))
      verify(enrolmentStoreProxyService, never()).enrolmentsForGroup(any())(meq(hc))
      verify(sessionCache, never()).saveEori(any())(meq(hc))
    }

    "fetch eori from user enrolments if eori not present in session cache" in {
      when(
        sessionCache
          .eori(any())
      ).thenReturn(Future.successful(None))
      when(
        sessionCache
          .saveGroupEnrolment(any())(any())
      ).thenReturn(Future.successful(true))
      val enrolments = Set(Enrolment("HMRC-CUS-ORG").withIdentifier("EORINumber", eori.id))
      await(groupEnrolmentExtractor.getEori(loggedInUser(enrolments))(hc))
      verify(enrolmentStoreProxyService, never()).enrolmentsForGroup(any())(meq(hc))
      verify(sessionCache).saveEori(any())(meq(hc))
    }

    "fetch eori from group enrolments if eori not present in session cache" in {
      when(enrolmentStoreProxyService.enrolmentsForGroup(any())(any()))
        .thenReturn(Future.successful(List(enrolmentResponse)))
      when(
        sessionCache
          .eori(any())
      ).thenReturn(Future.successful(None))
      when(
        sessionCache
          .saveGroupEnrolment(any())(any())
      ).thenReturn(Future.successful(true))
      val enrolments = Set(Enrolment("HMRC-CUS-ORG").withIdentifier("NINO", "NINO"))
      await(groupEnrolmentExtractor.getEori(loggedInUser(enrolments))(hc))
      verify(enrolmentStoreProxyService).enrolmentsForGroup(any())(meq(hc))
      verify(sessionCache).saveEori(any())(meq(hc))
    }

    "return existing EORI for user and/or group" when {

      "user has enrolment with an EORI" in {

        val userEnrolments = Set(Enrolment("HMRC-TEST-ORG").withIdentifier("EORINumber", eori.id))

        val result = groupEnrolmentExtractor.existingEoriForUser(userEnrolments)

        result shouldBe Some(ExistingEori(eori.id, "HMRC-TEST-ORG"))
      }

      "user has no enrolment with EORI " in {

        val userEnrolments = Set(Enrolment("HMRC-NI").withIdentifier("NINO", nino.id))

        val result = groupEnrolmentExtractor.existingEoriForUser(userEnrolments)

        result shouldBe None
      }

      "user has no enrolments" in {

        val userEnrolments: Set[Enrolment] = Set.empty

        val result = groupEnrolmentExtractor.existingEoriForUser(userEnrolments)

        result shouldBe None
      }
    }
  }

}
