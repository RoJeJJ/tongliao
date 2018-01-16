-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
ALTER PROCEDURE [dbo].[VerifyAccounts]
	-- Add the parameters for the stored procedure here
	@wOpenid NVARCHAR(MAX),
	@wNickname NVARCHAR(50),
	@wSex TINYINT,
	@avator NVARCHAR(MAX),
	@wIp NVARCHAR(33)
AS
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;

	DECLARE @wUserID INT
	DECLARE @regGrantCard BIGINT
	DECLARE @state INT
    -- Insert statements for procedure here
	SELECT @state = StatusValue FROM SystemStatusInfo WHERE StatusName = 'LoginEnable';
	IF (@state = 1)
		RETURN 1;
	IF NOT EXISTS (SELECT userid FROM dbo.user_info WHERE openid = @wOpenid)
		BEGIN
			SELECT @regGrantCard = StatusValue FROM dbo.SystemStatusInfo WHERE StatusName = N'GrantCardCount'
			INSERT INTO dbo.user_info
			        ( openid ,
			          nickname ,
			          gender ,
			          avatar ,
			          ip ,
					  card,
			          lockcard ,
					  LogedIn,
			          RegisterDate ,
			          LastLogonDate ,
			          LastLogoutDate 
			        )
			VALUES  ( @wOpenid , 
			          @wNickname , 
			          @wSex , 
					  @avator,
			          @wIP ,
					  @regGrantCard,
			          0 ,
					  0,
			          GETDATE() , 
			          NULL ,
			          NULL 
			        )
		END
	ELSE
		BEGIN
			UPDATE dbo.user_info SET nickname = @wNickname,gender = @wSex,avatar=@avator,ip=@wIp  WHERE openid = @wOpenid
		END

	DECLARE @uid INT
	DECLARE @nick NVARCHAR(50)
	DECLARE @gender TINYINT
	DECLARE @faceurl NVARCHAR(MAX)
	DECLARE @ip NVARCHAR(33)
	DECLARE @nullity TINYINT
	DECLARE @card BIGINT
	DECLARE @pid INT
	DECLARE  @vip BIT


	SELECT @uid=UserID,@nick=nickname,@gender=gender,@faceurl=avatar,@IP=ip,@nullity=Nullity,@card=card,@pid =parentId  FROM dbo.user_info WHERE openid = @wOpenid
	IF (exists(SELECT * FROM vvip WHERE uid=@uid))
		SELECT @vip = -1;
	ELSE
		SELECT @vip = 0;
	SELECT @uid AS uid,@nick AS nick,@gender AS gender,@faceurl AS faceurl,@ip AS ip,@card AS card,@pid AS pid,@nullity AS nullity,@vip AS vip
	RETURN 0;
END
GO

