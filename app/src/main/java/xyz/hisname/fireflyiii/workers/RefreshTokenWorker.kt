package xyz.hisname.fireflyiii.workers

import android.accounts.AccountManager
import android.content.Context
import androidx.work.*
import xyz.hisname.fireflyiii.data.local.account.AuthenticatorManager
import xyz.hisname.fireflyiii.data.remote.api.OAuthService

class RefreshTokenWorker(private val context: Context, workerParameters: WorkerParameters): BaseWorker(context, workerParameters)  {

    private val accManager by lazy { AuthenticatorManager(AccountManager.get(context)) }

    override fun doWork(): Result {
        val execution = service()?.execute()
        return if(execution != null){
            val responseBody = execution.body()
            if(responseBody != null){
                accManager.apply {
                    accessToken = responseBody.access_token
                    refreshToken = responseBody.refresh_token
                    tokenExpiry = responseBody.expires_in
                }
                Result.SUCCESS
            } else {
                Result.FAILURE
            }
        } else {
            Result.RETRY
        }
    }

    private fun service() = genericService?.create(OAuthService::class.java)?.getRefreshToken("refresh_token",
            accManager.refreshToken, accManager.clientId, accManager.secretKey)

}