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
import uk.gov.hmrc.xieoricommoncomponentfrontend.cache.UserAnswersCache
import uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.auth.{AuthAction, EnrolmentExtractor}
import uk.gov.hmrc.xieoricommoncomponentfrontend.domain.LoggedInUserWithEnrolments
import uk.gov.hmrc.xieoricommoncomponentfrontend.forms.ManualPBEAddressFormProvider
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.forms.ManualPBEAddress
import uk.gov.hmrc.xieoricommoncomponentfrontend.viewmodels.AddressViewModel
import uk.gov.hmrc.xieoricommoncomponentfrontend.views.html.manual_pbe_address

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ManualPBEAddressController @Inject() (
  authAction: AuthAction,
  manualPBEAddressView: manual_pbe_address,
  formProvider: ManualPBEAddressFormProvider,
  mcc: MessagesControllerComponents,
  userAnswersCache: UserAnswersCache
)(implicit val ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with EnrolmentExtractor {

  private val form = formProvider()

  def onPageLoad: Action[AnyContent] =
    authAction.ggAuthorisedUserWithEnrolmentsAction {
      implicit request => _: LoggedInUserWithEnrolments =>
        userAnswersCache.getAdddressDetails() map {
          case Some(pbeAddressDetails) =>
            Ok(manualPBEAddressView(form.fill(fetchAddressDetail(pbeAddressDetails))))
          case None => Ok(manualPBEAddressView(form))
        }
    }

  def submit: Action[AnyContent] =
    authAction.ggAuthorisedUserWithEnrolmentsAction { implicit request => loggedInUser: LoggedInUserWithEnrolments =>
      form.bindFromRequest.fold(
        invalidForm => Future.successful(BadRequest(manualPBEAddressView(invalidForm))),
        validAddressParams =>
          loggedInUser.affinityGroup match {
            case Some(AffinityGroup.Organisation) =>
              userAnswersCache.cacheAddressDetails(toAddressModel(validAddressParams)).map { _ =>
                Redirect(
                  uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.XiEoriNotNeededController.eoriNotNeeded()
                )
              }
            case _ =>
              Future.successful(
                Redirect(
                  uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.XiEoriNotNeededController.eoriNotNeeded()
                )
              )

          }
      )
    }

  private def toAddressModel(validPBEAddressParams: ManualPBEAddress): AddressViewModel = {
    val line1       = validPBEAddressParams.line1
    val townCity    = validPBEAddressParams.townorcity
    val postCode    = Some(validPBEAddressParams.postcode)
    val countryCode = "GB"
    AddressViewModel(line1, townCity, postCode, countryCode)
  }

  private def fetchAddressDetail(addressViewModel: AddressViewModel): ManualPBEAddress = {
    val line1    = addressViewModel.street
    val townCity = addressViewModel.city
    val postCode: String = addressViewModel.postcode match {
      case None            => ""
      case Some(p: String) => p
    }
    val country = "GB"
    ManualPBEAddress.apply(line1, townCity, postCode, Option(country))
  }

}
