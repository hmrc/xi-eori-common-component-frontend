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

package uk.gov.hmrc.xieoricommoncomponentfrontend.controllers

import play.api.i18n.I18nSupport
import play.api.mvc._
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.xieoricommoncomponentfrontend.cache.{SessionCache, UserAnswersCache}
import uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.auth.{AuthAction, EnrolmentExtractor}
import uk.gov.hmrc.xieoricommoncomponentfrontend.domain.LoggedInUserWithEnrolments
import uk.gov.hmrc.xieoricommoncomponentfrontend.forms.ManualContactAddressFormProvider
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.forms.ManualContactAddress
import uk.gov.hmrc.xieoricommoncomponentfrontend.services.countries.Countries
import uk.gov.hmrc.xieoricommoncomponentfrontend.views.html.manual_contact_address

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ManualContactAddressController @Inject() (
  authAction: AuthAction,
  manualContactAddressView: manual_contact_address,
  formProvider: ManualContactAddressFormProvider,
  mcc: MessagesControllerComponents,
  userAnswersCache: UserAnswersCache,
  sessionCache: SessionCache
)(implicit val ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with EnrolmentExtractor {

  private val form = formProvider()

  val (countries, picker) = Countries.getCountryParametersForAllCountries()

  def onPageLoad: Action[AnyContent] =
    authAction.ggAuthorisedUserWithEnrolmentsAction {
      implicit request => _: LoggedInUserWithEnrolments =>
        Future.successful(Ok(manualContactAddressView(form, countries, picker)))

    }

  def reviewPageLoad(): Action[AnyContent] =
    authAction.ggAuthorisedUserWithEnrolmentsAction {
      implicit request => _: LoggedInUserWithEnrolments =>
        Future.successful(Ok(manualContactAddressView(form, countries, picker)))

    }

  def submit: Action[AnyContent] =
    authAction.ggAuthorisedUserWithEnrolmentsAction { implicit request => loggedInUser: LoggedInUserWithEnrolments =>
      form.bindFromRequest.fold(
        invalidForm => Future.successful(BadRequest(manualContactAddressView(invalidForm, countries, picker))),
        validAddressParams =>
          loggedInUser.affinityGroup match {
            case Some(AffinityGroup.Organisation) =>
              for {
                _ <- userAnswersCache.cacheAddressDetails(ManualContactAddress.toAddressModel(validAddressParams))
                _ <- sessionCache.clearAddressLookupParams
              } yield Redirect(
                uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.PBEConfirmAddressController.onPageLoad()
              )
            case _ =>
              Future.successful(
                Redirect(
                  uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.XiEoriNotNeededController.eoriNotNeeded()
                )
              )

          }
      )
    }

}
