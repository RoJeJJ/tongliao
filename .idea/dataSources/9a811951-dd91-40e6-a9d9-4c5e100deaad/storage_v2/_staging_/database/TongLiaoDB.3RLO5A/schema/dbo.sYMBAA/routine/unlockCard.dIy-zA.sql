-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
ALTER PROCEDURE [dbo].[unlockCard]
	-- Add the parameters for the stored procedure here
	@id INT,
	@card INT
AS
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;

    IF(EXISTS(SELECT * FROM dbo.user_info WHERE userid=@id))
	BEGIN
		UPDATE dbo.user_info SET lockcard=lockcard-@card WHERE userid=@id AND lockcard>=@card;
		IF(@@ERROR=0 AND @@ROWCOUNT=1)
			RETURN 0;
	END
	RETURN 1;
END
GO

