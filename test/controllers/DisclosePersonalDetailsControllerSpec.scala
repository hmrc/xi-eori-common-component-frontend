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

package controllers

import common.pages.RegistrationPage
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import play.api.{inject, Application}
import uk.gov.hmrc.auth.core.{AffinityGroup, AuthConnector}
import uk.gov.hmrc.xieoricommoncomponentfrontend.cache.{SessionCache, UserAnswersCache}
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.forms.DisclosePersonalDetails
import util.BaseSpec
import util.builders.AuthBuilder.withAuthorisedUser
import util.builders.SessionBuilder

import scala.concurrent.Future

class DisclosePersonalDetailsControllerSpec extends BaseSpec {

  def app: Application = new GuiceApplicationBuilder().overrides(
    inject.bind[AuthConnector].to(mockAuthConnector),
    inject.bind[UserAnswersCache].to(mockUserAnswersCache),
    inject.bind[SessionCache].to(mockSessionCache)
  ).configure("auditing.enabled" -> "false", "metrics.jvm" -> false, "metrics.enabled" -> false).build()

  "Disclose Personal Details controller" should {
    "return OK and the correct view for a GET" in {

      running(app) {
        withAuthorisedUser(defaultUserId, mockAuthConnector, userAffinityGroup = AffinityGroup.Organisation)
        when(mockUserAnswersCache.getPersonalDataDisclosureConsent()(any())).thenReturn(Future.successful(None))
        val request = SessionBuilder.buildRequestWithSessionAndPath(
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.DisclosePersonalDetailsController.onPageLoad().url
        )

        val result = route(app, request).get

        val page = RegistrationPage(contentAsString(result))

        page.title should startWith("Do you want to include the name and address on the EORI checker?")
      }
    }

    "populate View if userAnswersCache has session data" in {
      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector, userAffinityGroup = AffinityGroup.Organisation)
        when(mockUserAnswersCache.getPersonalDataDisclosureConsent()(any())).thenReturn(Future.successful(Some(true)))
        val request = SessionBuilder.buildRequestWithSessionAndPath(
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.DisclosePersonalDetailsController.onPageLoad().url
        )

        val result = route(app, request).get

        val page = RegistrationPage(contentAsString(result))
        page.getElementValue("//*[@id='value']") shouldBe "yes"
      }
    }

    "redirect to the next page when Organisation group with valid data is submitted" in {
      running(app) {
        withAuthorisedUser(defaultUserId, mockAuthConnector, userAffinityGroup = AffinityGroup.Organisation)
        when(mockUserAnswersCache.getPersonalDataDisclosureConsent()(any())).thenReturn(Future.successful(None))
        when(mockUserAnswersCache.cacheConsentToDisclosePersonalDetails(any())(any())).thenReturn(
          Future.successful(true)
        )
        val request = SessionBuilder.buildPostRequestWithSessionAndPathAndFormValues(
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.DisclosePersonalDetailsController.submit().url,
          Map("value" -> DisclosePersonalDetails.values.head.toString)
        )

        val result = route(app, request).get
        status(result) shouldBe SEE_OTHER
        redirectLocation(
          result
        ).get shouldBe uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.SicCodeController.onPageLoad().url
      }

    }

    "redirect to the next page when Affinity group is individual" in {
      running(app) {
        withAuthorisedUser(defaultUserId, mockAuthConnector, userAffinityGroup = AffinityGroup.Individual)

        val request = SessionBuilder.buildPostRequestWithSessionAndPathAndFormValues(
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.DisclosePersonalDetailsController.submit().url,
          Map("value" -> DisclosePersonalDetails.values.head.toString)
        )

        val result = route(app, request).get
        status(result) shouldBe SEE_OTHER
        redirectLocation(
          result
        ).get shouldBe uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.XiEoriNotNeededController.eoriNotNeeded().url

      }
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      running(app) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)

        val request = SessionBuilder.buildPostRequestWithSessionAndPathAndFormValues(
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.DisclosePersonalDetailsController.submit().url,
          Map("value" -> "")
        )

        val result = route(app, request).get
        status(result) shouldBe BAD_REQUEST

        val page = RegistrationPage(contentAsString(result))
        page.errors should startWith("Tell us if you want to include the name and address on the EORI checker")
      }
    }
  }
}
